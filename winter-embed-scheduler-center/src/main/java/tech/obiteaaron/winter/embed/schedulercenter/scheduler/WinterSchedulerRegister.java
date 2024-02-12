package tech.obiteaaron.winter.embed.schedulercenter.scheduler;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import tech.obiteaaron.winter.common.tools.id.TimestampGenerator;
import tech.obiteaaron.winter.common.tools.json.JsonUtil;
import tech.obiteaaron.winter.common.tools.system.SystemStatus;
import tech.obiteaaron.winter.common.tools.threadpool.ThreadUtil;
import tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap;
import tech.obiteaaron.winter.embed.rpc.regesiter.ProviderConfig;
import tech.obiteaaron.winter.embed.schedulercenter.*;
import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJob;
import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJobStatusEnum;
import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJobTimeTypeEnum;
import tech.obiteaaron.winter.embed.schedulercenter.repository.WinterJobRepository;
import tech.obiteaaron.winter.embed.schedulercenter.timing.TimeStrategy;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 注册器
 */
@Slf4j
public class WinterSchedulerRegister {

    private final ScheduledExecutorService REGISTER_POOL = Executors.newSingleThreadScheduledExecutor();

    private final LinkedBlockingQueue<WinterJob> waitRegisterList = new LinkedBlockingQueue<>();

    @Setter
    private WinterJobRepository winterJobRepository;

    @Setter
    private WinterSchedulerCenter winterSchedulerCenter;

    private final TimestampGenerator timestampGenerator = new TimestampGenerator();

    public WinterJob addWinterJob(JobProcessor jobProcessor) {
        WinterJob winterJob = toWinterJob(jobProcessor);
        return addWinterJob(winterJob);
    }

    public WinterJob addWinterJob(WinterJob winterJob) {
        waitRegisterList.add(winterJob);
        return winterJob;
    }

    private WinterJob toWinterJob(JobProcessor jobProcessor) {
        WinterJob winterJob = new WinterJob();
        winterJob.setId(timestampGenerator.generate());
        winterJob.setGmtCreate(new Date());
        winterJob.setGmtModified(new Date());
        winterJob.setName(jobProcessor.getClass().getName());
        winterJob.setClassName(jobProcessor.getClass().getName());
        winterJob.setJobProcessor(jobProcessor);
        winterJob.setJobType(resolveJobType(jobProcessor));
        winterJob.setTimeType(resolveTimeType(jobProcessor));
        winterJob.setTimeExpression(resolveTimeExpression(winterJob, jobProcessor));
        winterJob.setBeginTime(null);
        winterJob.setEndTime(null);
        winterJob.setNextTriggerTime(resolveNextTriggerTime(winterJob, jobProcessor));
        winterJob.setStatus(WinterJobStatusEnum.NORMAL.name());
        winterJob.setFeatures(null);
        winterJob.setExtraInfo(null);
        return winterJob;
    }

    private String resolveJobType(JobProcessor jobProcessor) {
        if (jobProcessor instanceof MapReduceJobProcessor) {
            return MapReduceJobProcessor.class.getName();
        } else if (jobProcessor instanceof MapJobProcessor) {
            return MapJobProcessor.class.getName();
        } else if (jobProcessor instanceof LongTimeJobProcessor) {
            return LongTimeJobProcessor.class.getName();
        } else if (jobProcessor instanceof SimpleJobProcessor) {
            return SimpleJobProcessor.class.getName();
        } else if (jobProcessor instanceof ComplexDAGJobProcessor) {
            return ComplexDAGJobProcessor.class.getName();
        } else {
            throw new UnsupportedOperationException("unsupported type " + jobProcessor.getClass().getName());
        }
    }

    private String resolveTimeType(JobProcessor jobProcessor) {
        WinterScheduled winterScheduled = jobProcessor.getClass().getAnnotation(WinterScheduled.class);
        Objects.requireNonNull(winterScheduled, "@WinterScheduled cannot be null");
        if (winterScheduled.fixedDelay() > 0) {
            return WinterJobTimeTypeEnum.FIXED_DELAY.name();
        }
        if (winterScheduled.fixedRate() > 0) {
            return WinterJobTimeTypeEnum.FIXED_RATE.name();
        }
        if (StringUtils.isNotBlank(winterScheduled.cron())) {
            return WinterJobTimeTypeEnum.CRON.name();
        }
        throw new UnsupportedOperationException("@WinterScheduled resolveTimeType failed");
    }

    private String resolveTimeExpression(WinterJob winterJob, JobProcessor jobProcessor) {
        String timeType = winterJob.getTimeType();
        TimeStrategy timeStrategy = TimeStrategy.resolveTimeStrategy(timeType);
        return timeStrategy.parseTimeExpression(winterJob, jobProcessor);
    }

    private Date resolveNextTriggerTime(WinterJob winterJob, JobProcessor jobProcessor) {
        Objects.requireNonNull(winterJob.getTimeType(), "timeType cannot be null");
        String timeType = winterJob.getTimeType();
        TimeStrategy timeStrategy = TimeStrategy.resolveTimeStrategy(timeType);
        return timeStrategy.nextTriggerTime(winterJob);
    }

    public void start() {
        REGISTER_POOL.scheduleWithFixedDelay(ThreadUtil.wrapperForNoThrowable(() -> {
            while (true) {
                if (!SystemStatus.running) {
                    return;
                }
                WinterJob poll = waitRegisterList.poll();
                if (poll == null) {
                    return;
                }
                doAddWinterJob(poll);
            }
        }), 1, 10, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                REGISTER_POOL.shutdown();
            }
        }));
    }

    public void doAddWinterJob(WinterJob winterJob) {
        try {
            boolean save = winterJobRepository.save(winterJob);
            if (!save) {
                log.warn("register job failed winterJob = {}", JsonUtil.toJsonString(winterJob));
            }
            // 如果是Map类任务，需要将实现的类注册到RPC中，在Map时分发子任务到可用的机器上
            if (winterJob.getJobProcessor() instanceof MapJobProcessor) {
                registerMapJobProcessorTaskCall(winterJob);
            }
        } catch (Throwable t) {
            log.error("register job exception winterJob = {}", JsonUtil.toJsonString(winterJob), t);
        }
    }

    private void registerMapJobProcessorTaskCall(WinterJob winterJob) throws NoSuchMethodException {
        JobProcessor jobProcessor = winterJob.getJobProcessor();
        if (!(jobProcessor instanceof MapJobProcessor)) {
            return;
        }
        Class<? extends JobProcessor> jobProcessorClass = jobProcessor.getClass();
        WinterRpcBootstrap winterRpcBootstrap = WinterSchedulerCenter.INSTANCE.getWinterRpcBootstrap();
        // register到RPC里面
        ProviderConfig providerConfig = ProviderConfig.builder()
                .interfaceClass(jobProcessorClass)
                .interfaceName(jobProcessorClass.getName())
                .interfaceImpl(jobProcessor)
                .version("1.0.0")
                .group("WinterScheduler")
                .tags(null)
                .build();
        winterRpcBootstrap.addProviderConfig(providerConfig);
    }
}

package tech.obiteaaron.winter.embed.schedulercenter.scheduler;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import tech.obiteaaron.winter.common.tools.id.TimestampGenerator;
import tech.obiteaaron.winter.common.tools.json.JsonUtil;
import tech.obiteaaron.winter.common.tools.system.SystemStatus;
import tech.obiteaaron.winter.common.tools.threadpool.MutableThreadPoolExecutorFactory;
import tech.obiteaaron.winter.embed.schedulercenter.JobContext;
import tech.obiteaaron.winter.embed.schedulercenter.JobProcessor;
import tech.obiteaaron.winter.embed.schedulercenter.WinterSchedulerCenter;
import tech.obiteaaron.winter.embed.schedulercenter.executor.BeanParser;
import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJob;
import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJobInstance;
import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJonStatusEnum;
import tech.obiteaaron.winter.embed.schedulercenter.powerjob.timewheel.holder.InstanceTimeWheelService;
import tech.obiteaaron.winter.embed.schedulercenter.repository.WinterJobInstanceRepository;
import tech.obiteaaron.winter.embed.schedulercenter.repository.WinterJobInstanceTaskRepository;
import tech.obiteaaron.winter.embed.schedulercenter.repository.WinterJobRepository;
import tech.obiteaaron.winter.embed.schedulercenter.repository.request.WinterJobQuery;
import tech.obiteaaron.winter.embed.schedulercenter.timing.TimeStrategy;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 调度器
 */
@Slf4j
public class WinterSchedulerDispatcher {

    private final ExecutorService SCHEDULER_POOL = MutableThreadPoolExecutorFactory.newCallerRunPool("WinterSchedulerCenter#Scheduler#" + this.hashCode(), () -> 2, 0);

    @Setter
    private WinterJobRepository winterJobRepository;

    @Setter
    private WinterJobInstanceRepository winterJobInstanceRepository;

    @Setter
    private WinterJobInstanceTaskRepository winterJobInstanceTaskRepository;

    @Setter
    private BeanParser beanParser;

    @Setter
    private WinterSchedulerCenter winterSchedulerCenter;

    private final TimestampGenerator timestampGenerator = new TimestampGenerator();
    /**
     * 后台调度任务的执行周期，也就是最小间隔周期，周期执行任务的时间间隔最小也不会小于这个时间。
     */
    private int schedulerIntervalSecond = 1;

    public void start() {
        SCHEDULER_POOL.submit(() -> {
            // 后台执行调度，需要一些默认的线程持续运行以确保定时任务会被调度到
            // 查询出未来一段时间内需要调度的所有任务，然后放入时间轮调度
            while (true) {
                try {
                    if (!SystemStatus.running) {
                        return;
                    }
                    long now = System.currentTimeMillis();
                    WinterJobQuery query = new WinterJobQuery();
                    query.setOnlyNeedExecuting(true);
                    query.setTimeDeviationSecond(schedulerIntervalSecond);

                    List<WinterJob> winterJobs = winterJobRepository.queryAll(query);
                    for (WinterJob winterJob : winterJobs) {
                        processWinterJob(winterJob, null);
                    }
                    for (WinterJob winterJob : winterJobs) {
                        refreshWinterJob(winterJob);
                    }
                    TimeUnit.MILLISECONDS.sleep(now + schedulerIntervalSecond * 1000L - System.currentTimeMillis());
                } catch (Throwable t) {
                    log.error("WinterScheduler WinterSchedulerExecutor Exception", t);
                }
            }
        });
    }

    private void processWinterJob(WinterJob winterJob, String manualParams) {
        try {
            // 创建instance
            WinterJobInstance winterJobInstance = toWinterJobInstance(winterJob, manualParams);
            boolean save = winterJobInstanceRepository.save(winterJobInstance);
            if (!save) {
                log.error("WinterScheduler WinterSchedulerExecutor processWinterJob instance saved failed winterJob = {}", JsonUtil.toJsonString(winterJob));
                return;
            }
            // 推入时间轮
            Date nextTriggerTime = winterJob.getNextTriggerTime();
            InstanceTimeWheelService.schedule(winterJobInstance.getId(), nextTriggerTime.getTime() - System.currentTimeMillis(), () -> {
                JobProcessor jobProcessor = beanParser.parse(winterJob);
                if (winterJobInstance.getJobProcessor() == null) {
                    winterJobInstance.setJobProcessor(jobProcessor);
                }

                JobContext jobContext = new JobContext();
                jobContext.setJobId(winterJobInstance.getJobId());
                jobContext.setInstanceId(winterJobInstance.getId());
                jobContext.setManualParams(manualParams);
                jobContext.setTaskType(JobContext.TaskTypeEnum.NORMAL.name());

                winterSchedulerCenter.getWinterSchedulerExecutor().run(winterJob, winterJobInstance, jobContext);
            });

        } catch (Throwable t) {
            log.error("WinterScheduler WinterSchedulerExecutor processWinterJob Exception", t);
        }
    }

    private WinterJobInstance toWinterJobInstance(WinterJob winterJob, String manualParams) {
        WinterJobInstance instance = new WinterJobInstance();
        instance.setId(timestampGenerator.generate());
        instance.setGmtCreate(winterJob.getGmtCreate());
        instance.setGmtModified(winterJob.getGmtModified());
        instance.setJobId(winterJob.getId());
        instance.setName(winterJob.getName());
        instance.setBeginTime(winterJob.getBeginTime());
        instance.setEndTime(winterJob.getEndTime());
        instance.setClassName(winterJob.getClassName());
        instance.setJobProcessor(winterJob.getJobProcessor());
        instance.setPeriodTime(winterJob.getNextTriggerTime());
        instance.setExtraInfo(winterJob.getExtraInfo());
        instance.setManualParams(manualParams);

        return instance;
    }

    private void refreshWinterJob(WinterJob winterJob) {
        TimeStrategy timeStrategy = TimeStrategy.resolveTimeStrategy(winterJob.getTimeType());
        Date nextTriggerTime = timeStrategy.nextTriggerTime(winterJob);
        winterJob.setNextTriggerTime(nextTriggerTime);
        winterJob.setGmtModified(new Date());
        if (winterJob.getNextTriggerTime() == null) {
            winterJob.setStatus(WinterJonStatusEnum.STOPPED.name());
        }
        boolean save = winterJobRepository.save(winterJob);
        if (!save) {
            log.warn("refresh job failed winterJob = {}", JsonUtil.toJsonString(winterJob));
        }
    }

    /**
     * 手动触发
     *
     * @param winterJob    Job
     * @param manualParams 自定义参数
     */
    public void triggerManual(WinterJob winterJob, String manualParams) {
        // 外部自行将nextTriggerTime设置为now即可，或者随意设置需要的时间
        processWinterJob(winterJob, manualParams);
    }
}

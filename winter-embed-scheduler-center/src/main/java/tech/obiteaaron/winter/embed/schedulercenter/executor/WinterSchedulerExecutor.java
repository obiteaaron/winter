package tech.obiteaaron.winter.embed.schedulercenter.executor;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import tech.obiteaaron.winter.common.tools.json.JsonUtils;
import tech.obiteaaron.winter.common.tools.lock.Lock;
import tech.obiteaaron.winter.common.tools.lock.Locks;
import tech.obiteaaron.winter.common.tools.system.SystemStatus;
import tech.obiteaaron.winter.common.tools.threadpool.MutableThreadPoolExecutorFactory;
import tech.obiteaaron.winter.common.tools.threadpool.ThreadUtils;
import tech.obiteaaron.winter.embed.schedulercenter.JobContext;
import tech.obiteaaron.winter.embed.schedulercenter.JobProcessor;
import tech.obiteaaron.winter.embed.schedulercenter.JobResult;
import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJob;
import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJobInstance;
import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJobInstanceStatusEnum;
import tech.obiteaaron.winter.embed.schedulercenter.repository.WinterJobInstanceRepository;

import java.util.Date;
import java.util.concurrent.ExecutorService;

/**
 * 执行器
 */
@Slf4j
public class WinterSchedulerExecutor {
    /**
     * 工作线程池大小
     */
    @Setter
    @Getter
    private int poolSize = 256;

    @Setter
    private WinterJobInstanceRepository winterJobInstanceRepository;

    private final ExecutorService EXECUTOR_POOL = MutableThreadPoolExecutorFactory.newCallerRunPool("WinterSchedulerCenter#Executor#" + this.hashCode(), () -> poolSize, 0);

    public WinterSchedulerExecutor() {
        ThreadUtils.registerForShutdown(EXECUTOR_POOL);
    }

    public void run(WinterJob winterJob, WinterJobInstance winterJobInstance, JobContext jobContext) {
        EXECUTOR_POOL.submit(() -> {
            if (!SystemStatus.running) {
                return;
            }
            doRun(winterJob, winterJobInstance, jobContext);
        });
    }

    private void doRun(WinterJob winterJob, WinterJobInstance winterJobInstance, JobContext jobContext) {
        // 在这里提供加锁选主的功能，而不是在具体Job的实现里面，相当于这里是Server端的实现，Job的实现是客户端
        // Job不支持多实例，只能单实例运行
        String lockKey = "WinterScheduler:Job:" + winterJob.getId();
        try (Lock lock = Locks.newRedisLock(lockKey)) {
            if (!lock.tryLock()) {
                log.warn("WinterSchedulerExecutor tryLock failed, ignore executing lockKey={}", lockKey);
                // 更新实例状态为失败
                winterJobInstance.setEndTime(new Date());
                winterJobInstance.setStatus(WinterJobInstanceStatusEnum.FAILED.name());
                winterJobInstance.setMessage("WinterSchedulerExecutor tryLock failed");
                return;
            }
            // 保存实例的执行结果
            boolean save1 = winterJobInstanceRepository.save(winterJobInstance);
            if (!save1) {
                log.error("WinterScheduler WinterSchedulerExecutor processWinterJob instance running saved failed. winterInstanceJob = {}", JsonUtils.toJsonString(winterJobInstance));
                return;
            }

            JobProcessor jobProcessor = winterJobInstance.getJobProcessor();
            JobResult jobResult = jobProcessor.process(jobContext);

            // 更新实例状态为完成，由于是本机执行，所以执行到此处则一定是执行结束了，和C/S架构不同。
            if (jobResult != null && !jobResult.isSuccess()) {
                winterJobInstance.setEndTime(new Date());
                winterJobInstance.setStatus(WinterJobInstanceStatusEnum.FAILED.name());
                winterJobInstance.setMessage(jobResult.getMessage());
            } else {
                winterJobInstance.setEndTime(new Date());
                winterJobInstance.setStatus(WinterJobInstanceStatusEnum.SUCCEED.name());
                winterJobInstance.setMessage("");
            }
        } catch (Exception e) {
            log.error("WinterScheduler WinterSchedulerExecutor processWinterJob instance running exception. winterInstanceJob = {}", JsonUtils.toJsonString(winterJobInstance), e);
            winterJobInstance.setEndTime(new Date());
            winterJobInstance.setStatus(WinterJobInstanceStatusEnum.FAILED.name());
            winterJobInstance.setMessage(e.toString());
        } finally {
            // 保存实例的执行结果
            boolean save2 = winterJobInstanceRepository.save(winterJobInstance);
            if (!save2) {
                log.error("WinterScheduler WinterSchedulerExecutor processWinterJob instance result saved failed. winterInstanceJob = {}", JsonUtils.toJsonString(winterJobInstance));
                return;
            }
        }
    }
}

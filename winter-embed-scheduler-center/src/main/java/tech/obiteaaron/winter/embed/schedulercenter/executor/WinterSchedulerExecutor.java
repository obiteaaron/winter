package tech.obiteaaron.winter.embed.schedulercenter.executor;

import lombok.extern.slf4j.Slf4j;
import tech.obiteaaron.winter.common.tools.lock.Lock;
import tech.obiteaaron.winter.common.tools.lock.Locks;
import tech.obiteaaron.winter.common.tools.threadpool.MutableThreadPoolExecutorFactory;
import tech.obiteaaron.winter.embed.schedulercenter.JobContext;
import tech.obiteaaron.winter.embed.schedulercenter.JobProcessor;
import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJob;
import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJobInstance;

import java.util.concurrent.ExecutorService;

/**
 * 执行器
 */
@Slf4j
public class WinterSchedulerExecutor {

    private final ExecutorService EXECUTOR_POOL = MutableThreadPoolExecutorFactory.newCallerRunPool("WinterSchedulerCenter#Executor#" + this.hashCode(), () -> 256, 0);

    public void run(WinterJob winterJob, WinterJobInstance winterJobInstance, JobContext jobContext) {
        EXECUTOR_POOL.submit(() -> {
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
                // TODO 更新实例状态为失败
                return;
            }
            JobProcessor jobProcessor = winterJobInstance.getJobProcessor();
            jobProcessor.process(jobContext);
            // TODO 更新实例状态为完成（需要根据任务类型而定，Simple的直接更新为完成，LongTime、Map、MapReduce的更新为执行中，等待上报状态）
        }
    }
}

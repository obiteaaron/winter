package tech.obiteaaron.winter.embed.schedulercenter;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import tech.obiteaaron.winter.common.tools.threadpool.MutableThreadPoolExecutorFactory;
import tech.obiteaaron.winter.common.tools.threadpool.ThreadUtil;
import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJob;
import tech.obiteaaron.winter.embed.schedulercenter.repository.WinterJobInstanceRepository;
import tech.obiteaaron.winter.embed.schedulercenter.repository.WinterJobInstanceTaskRepository;
import tech.obiteaaron.winter.embed.schedulercenter.repository.WinterJobRepository;

import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public final class WinterSchedulerCenter {
    /**
     * 单例，无需多例
     */
    public static final WinterSchedulerCenter INSTANCE = new WinterSchedulerCenter();

    private final ScheduledExecutorService REGISTER_POOL = Executors.newSingleThreadScheduledExecutor();

    private final ExecutorService SCHEDULER_POOL = MutableThreadPoolExecutorFactory.newCallerRunPool("WinterSchedulerCenter#Scheduler#" + this.hashCode(), () -> 16, 0);

    private final LinkedBlockingQueue<WinterJob> waitRegisterList = new LinkedBlockingQueue<>();

    private static final AtomicBoolean initialized = new AtomicBoolean();

    @Setter
    private WinterJobRepository winterJobRepository;

    @Setter
    private WinterJobInstanceRepository winterJobInstanceRepository;

    @Setter
    private WinterJobInstanceTaskRepository winterJobInstanceTaskRepository;

    private WinterSchedulerCenter() {
    }

    public void addWinterJob(JobProcessor jobProcessor) {
        WinterJob winterJob = toWinterJob(jobProcessor);
        addWinterJob(winterJob);
    }

    public void addWinterJob(WinterJob winterJob) {
        waitRegisterList.add(winterJob);
    }

    private WinterJob toWinterJob(JobProcessor jobProcessor) {
        // TODO 转换
        return null;
    }

    public void start() {
        if (!initialized.compareAndSet(false, true)) {
            return;
        }
        REGISTER_POOL.scheduleWithFixedDelay(() -> {
            ThreadUtil.wrapperForNoThrowable(() -> {
                while (true) {
                    WinterJob poll = waitRegisterList.poll();
                    if (poll == null) {
                        return;
                    }
                    // TODO 注册Job
                }
            });
        }, 1, 10, TimeUnit.SECONDS);
        SCHEDULER_POOL.submit(() -> {
            // TODO 后台执行调度，需要一些默认的线程持续运行以确保定时任务会被调度到
            // 查询出未来一段时间内需要调度的所有任务，然后放入时间轮调度
        });
    }


    public void doAddWinterJob(WinterJob winterJob) {
        try {
            winterJobRepository.save(winterJob);
            Date nextTriggerTime = calcNextTriggerTime(winterJob);
        } catch (Throwable t) {
            log.error("register job exception", t);
        }
    }

    private Date calcNextTriggerTime(WinterJob winterJob) {
        // TODO 转换
        return null;
    }

}

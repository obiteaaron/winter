package tech.obiteaaron.winter.embed.schedulercenter;

import tech.obiteaaron.winter.common.tools.system.SystemStatus;

import java.util.concurrent.TimeUnit;

/**
 * 常驻任务处理器
 * 执行一次后不退出，直到达成退出条件。比普通任务的优势是，可以通过{@link #sleepMillisecond()}动态调整暂停的时间，而不是被定时器限制死了。但也会带来一定的编程复杂性。
 */
public interface LongTimeJobProcessor extends JobProcessor {

    @Override
    default void doProcess(JobContext jobContext) {
        // 分阶段执行
        do {
            try {
                beforeRunOnce(jobContext);
                doProcessOnce(jobContext);
            } finally {
                afterRunOnce(jobContext);
            }
        } while (isLongTimeRunning(jobContext) && sleepOnce(jobContext));
    }

    default void beforeRunOnce(JobContext jobContext) {

    }

    void doProcessOnce(JobContext jobContext);

    default boolean isLongTimeRunning(JobContext jobContext) {
        return SystemStatus.running;
    }

    default void afterRunOnce(JobContext jobContext) {

    }

    default boolean sleepOnce(JobContext jobContext) {
        long startTime = System.currentTimeMillis();
        try {
            while (true) {
                long millisecond = sleepMillisecond();
                if (millisecond <= 1000) {
                    TimeUnit.MILLISECONDS.sleep(millisecond);
                    return true;
                } else {
                    long endTime = startTime + millisecond;
                    if (endTime > System.currentTimeMillis()) {
                        // 1秒后再尝试，方便外部唤醒
                        TimeUnit.MILLISECONDS.sleep(Math.min(endTime - System.currentTimeMillis(), 1000));
                    } else {
                        return true;
                    }
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    default long sleepMillisecond() {
        return TimeUnit.SECONDS.toMillis(5);
    }
}

package tech.obiteaaron.winter.embed.schedulercenter;

import tech.obiteaaron.winter.common.tools.system.SystemStatus;

import java.util.concurrent.TimeUnit;

/**
 * 常驻任务处理器
 * 执行一次后不退出，直到达成退出条件
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
            sleepOnce(jobContext);
        } while (isLongTimeRunning(jobContext));
    }

    default void beforeRunOnce(JobContext jobContext) {

    }

    void doProcessOnce(JobContext jobContext);

    default boolean isLongTimeRunning(JobContext jobContext) {
        return SystemStatus.running;
    }

    default void afterRunOnce(JobContext jobContext) {

    }

    default void sleepOnce(JobContext jobContext) {
        long startTime = System.currentTimeMillis();
        try {
            while (true) {
                long millisecond = sleepMillisecond();
                if (millisecond <= 1000) {
                    TimeUnit.MILLISECONDS.sleep(millisecond);
                    return;
                } else {
                    long endTime = startTime + millisecond;
                    if (endTime > System.currentTimeMillis()) {
                        // 1秒后再尝试，方便外部唤醒
                        TimeUnit.MILLISECONDS.sleep(Math.min(endTime - System.currentTimeMillis(), 1000));
                    } else {
                        return;
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

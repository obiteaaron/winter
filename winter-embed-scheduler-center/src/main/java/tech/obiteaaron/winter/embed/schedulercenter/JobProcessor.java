package tech.obiteaaron.winter.embed.schedulercenter;

/**
 * 基础接口，用来组合使用
 */
public interface JobProcessor {

    default void process(JobContext jobContext) {
        // 分阶段执行
        try {
            beforeRun(jobContext);
            doProcess(jobContext);
        } finally {
            afterRun(jobContext);
        }
    }

    default void beforeRun(JobContext jobContext) {

    }

    void doProcess(JobContext jobContext);

    default void afterRun(JobContext jobContext) {

    }
}

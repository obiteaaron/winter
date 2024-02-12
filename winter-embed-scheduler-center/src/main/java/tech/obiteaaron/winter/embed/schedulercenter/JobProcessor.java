package tech.obiteaaron.winter.embed.schedulercenter;

/**
 * 基础接口，用来组合使用
 */
public interface JobProcessor {

    default JobResult process(JobContext jobContext) {
        // 分阶段执行
        try {
            beforeRun(jobContext);
            return doProcess(jobContext);
        } finally {
            afterRun(jobContext);
        }
    }

    default void beforeRun(JobContext jobContext) {

    }

    JobResult doProcess(JobContext jobContext);

    default void afterRun(JobContext jobContext) {

    }
}

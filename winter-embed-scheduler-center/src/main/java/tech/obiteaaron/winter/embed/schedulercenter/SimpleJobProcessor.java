package tech.obiteaaron.winter.embed.schedulercenter;

/**
 * 简单任务处理器，执行一次后立即退出
 */
public interface SimpleJobProcessor extends JobProcessor {
    @Override
    void doProcess(JobContext jobContext);
}

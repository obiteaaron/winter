package tech.obiteaaron.winter.embed.schedulercenter;

/**
 * 基础接口，用来组合使用
 */
public interface JobProcessor {

    void process(JobContext jobContext);
}

package tech.obiteaaron.winter.embed.schedulercenter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WinterSchedulerCenterConfig {
    /**
     * 执行任务线程池大小
     */
    private int threadPoolSize = 256;
    /**
     * 延迟启动时长，单位毫秒
     */
    private int delayStartMillisecond = 0;

}

package tech.obiteaaron.winter.embed.schedulercenter.spring;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("tech.obiteaaron.winter.embed.schedulercenter")
@Getter
@Setter
public class WinterSchedulerCenterProperties {

    private boolean enable = true;
    /**
     * 执行任务线程池大小
     */
    private int threadPoolSize = 256;
}

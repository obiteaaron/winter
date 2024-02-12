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
    /**
     * 延迟启动时长，单位毫秒
     */
    private int delayStartMillisecond = 0;
    /**
     * 是否启动Map任务的RPC功能，启动后会在集群内调度，不启动则只在本机调度
     */
    private boolean enableMapJobClusterRpc = true;
    /**
     * 是否使用默认的RPC实例，如果没有使用负载均衡服务器，通常都是可以共用的
     * TODO 目前仅实现了共享实例，独享实例暂未实现
     */
    private boolean useDefaultWinterRpcBootstrap = true;
    /**
     * 存储类型：MEMORY、Mysql、JPA
     */
    private String repositoryType = "MEMORY";
}

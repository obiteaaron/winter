package tech.obiteaaron.winter.embed.rpc.spring;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("tech.obiteaaron.winter.embed.rpc")
@Getter
@Setter
public class WinterRpcProperties {

    private String applicationName = "default";

    private int port = 7080;

    private boolean httpsEnable = false;

    private String loadBalanceServer;

    private String ipPrefix;
    /**
     * 服务提供者默认支持的序列化类型，多个用逗号“,”隔开。
     * JSON可能存在兼容性问题，主要是为了简单测试使用，生产环境建议都是用hessian。
     */
    private String serializerType = "hessian";
    /**
     * 提供者线程池数量
     */
    private int providerThreadPoolSize = 10;
    /**
     * 消费者线程池数量（并行量）
     * 默认值10，高并发下用连接池性能比直连好一点。配置为0或小于0的数字，则不启用HTTP的连接池，采用直接调用的方式。
     * 注意：直连的模式下，如果QPS过高，会导致连接被占用完而出错，建议还是用连接池模式。
     */
    private int consumerThreadPoolSize = 10;
    /**
     * 客户端超时时间，毫秒
     */
    private int consumerTimeoutMilliSecond = 3000;
}

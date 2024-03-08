package tech.obiteaaron.winter.embed.rpc;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WinterRpcConfig {

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
     * 提供者线程池数量（vertx的worker线程数量），在RPC框架中起到分发请求的作用
     */
    private int providerThreadPoolSize = 10;
    /**
     * 提供者工作的线程池数量，对应实际执行业务逻辑的线程池数量，Async功能会基于此线程池执行并返回Future进行异步等待，执行耗时的请求
     */
    private int providerWorkerThreadPoolSize = 50;
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
    /**
     * 是否使用虚拟线程，需要Java21支持，且需要注意ThreadLocal的使用
     */
    private boolean useVirtualThread = false;

    private boolean logging = false;
}

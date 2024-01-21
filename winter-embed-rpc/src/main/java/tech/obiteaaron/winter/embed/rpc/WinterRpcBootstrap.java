package tech.obiteaaron.winter.embed.rpc;

import lombok.Getter;
import lombok.Setter;
import tech.obiteaaron.winter.embed.rpc.executing.ConsumerDispatcher;
import tech.obiteaaron.winter.embed.rpc.executing.ProviderDispatcher;
import tech.obiteaaron.winter.embed.rpc.regesiter.RegisterManager;
import tech.obiteaaron.winter.embed.rpc.server.VertxHttpServer;

/**
 * 支持多实例，各用各的，多实例之间相互隔离，互不影响，各注册各的服务提供者和消费者
 */
@Getter
@Setter
public class WinterRpcBootstrap {

    private VertxHttpServer vertxHttpServer;

    private RegisterManager registerManager;

    private ProviderDispatcher providerDispatcher;

    private ConsumerDispatcher consumerDispatcher;

    private int port;
    /**
     * 服务提供者默认支持的序列化类型，多个用逗号“,”隔开
     */
    private String defaultSerializerType = "hessian";
    /**
     * 服务提供者默认支持的序列化类型，多个用逗号“,”隔开
     */
    private String providerSerializerSupports = "hessian,json";

    /**
     * 服务消费者默认支持的序列化类型，多个用逗号“,”隔开
     */
    private String consumerSerializerSupports = "hessian,json";

    public void start() {
        vertxHttpServer.startHttpServer(port);
    }
}

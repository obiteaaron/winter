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

    public void start() {
        vertxHttpServer.startHttpServer(port);
    }
}

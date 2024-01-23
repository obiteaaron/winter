package tech.obiteaaron.winter.embed.rpc;

import lombok.Getter;
import lombok.Setter;
import tech.obiteaaron.winter.embed.rpc.executing.ConsumerDispatcher;
import tech.obiteaaron.winter.embed.rpc.executing.ProviderDispatcher;
import tech.obiteaaron.winter.embed.rpc.regesiter.ConsumerConfig;
import tech.obiteaaron.winter.embed.rpc.regesiter.ProviderConfig;
import tech.obiteaaron.winter.embed.rpc.regesiter.RegisterManager;
import tech.obiteaaron.winter.embed.rpc.router.ProviderRouter;
import tech.obiteaaron.winter.embed.rpc.server.VertxHttpServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * 支持多实例，各用各的，多实例之间相互隔离，互不影响，各注册各的服务提供者和消费者。
 * 初始化方法可以参考适配Spring的自动配置类。
 */
@Getter
@Setter
public class WinterRpcBootstrap {

    public static final ConcurrentHashMap<String, WinterRpcBootstrap> INSTANCE_MAP = new ConcurrentHashMap<>();

    private String name;

    private VertxHttpServer vertxHttpServer;

    private RegisterManager registerManager;

    private ProviderDispatcher providerDispatcher;

    private ConsumerDispatcher consumerDispatcher;

    private List<ProviderRouter> providerRouters = new ArrayList<>();
    /**
     * http和https可选，默认http，如果认为http不安全，可以开启https开关
     */
    private boolean httpsEnable = false;
    /**
     * 负载均衡服务，需要用HTTP或者HTTPS协议，用于覆盖服务端的访问地址或域名
     */
    private String loadBalanceServer;

    private int port = 7080;
    /**
     * 服务提供者默认支持的序列化类型，多个用逗号“,”隔开。
     * JSON可能存在兼容性问题，主要是为了简单测试使用，生产环境建议都是用hessian。
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

    private List<ConsumerConfig> consumerConfigList = new ArrayList<>();

    private List<ProviderConfig> providerConfigList = new ArrayList<>();

    private WinterRpcBootstrap(String name) {
        this.name = name;
    }

    public static WinterRpcBootstrap instance() {
        return instance("default");
    }

    public static WinterRpcBootstrap instance(String name) {
        Objects.requireNonNull(name);
        return INSTANCE_MAP.compute(name, new BiFunction<String, WinterRpcBootstrap, WinterRpcBootstrap>() {
            @Override
            public WinterRpcBootstrap apply(String s, WinterRpcBootstrap winterRpcBootstrap) {
                if (winterRpcBootstrap != null) {
                    return winterRpcBootstrap;
                }
                return new WinterRpcBootstrap(name);
            }
        });
    }

    public void init() {

    }

    public void start() {
        // 先启动监听服务
        vertxHttpServer.startHttpServer(port);
        // 真正注册
        for (ProviderConfig providerConfig : providerConfigList) {
            getRegisterManager().register(providerConfig);
        }
        // 真正注册
        for (ConsumerConfig consumerConfig : consumerConfigList) {
            getRegisterManager().subscribe(consumerConfig);
        }
    }

    public String getHttpProtocol() {
        return httpsEnable ? "https" : "http";
    }
}

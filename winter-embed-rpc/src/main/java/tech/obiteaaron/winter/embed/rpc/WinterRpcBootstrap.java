package tech.obiteaaron.winter.embed.rpc;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import tech.obiteaaron.winter.embed.rpc.constant.IpAddressUtil;
import tech.obiteaaron.winter.embed.rpc.executing.ConsumerDispatcher;
import tech.obiteaaron.winter.embed.rpc.executing.ProviderDispatcher;
import tech.obiteaaron.winter.embed.rpc.filter.RpcFilter;
import tech.obiteaaron.winter.embed.rpc.regesiter.ConsumerConfig;
import tech.obiteaaron.winter.embed.rpc.regesiter.ProviderConfig;
import tech.obiteaaron.winter.embed.rpc.regesiter.RegisterManager;
import tech.obiteaaron.winter.embed.rpc.router.ProviderRouter;
import tech.obiteaaron.winter.embed.rpc.scheduler.ProviderWatchDog;
import tech.obiteaaron.winter.embed.rpc.server.VertxHttpServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支持多实例，各用各的，多实例之间相互隔离，互不影响，各注册各的服务提供者和消费者。
 * 初始化方法可以参考适配Spring的自动配置类。
 */
@Getter
public class WinterRpcBootstrap {

    public static final ConcurrentHashMap<String, WinterRpcBootstrap> INSTANCE_MAP = new ConcurrentHashMap<>();

    private String name;
    /**
     * http和https可选，默认http，如果认为http不安全，可以开启https开关
     */
    private boolean httpsEnable = false;
    /**
     * 负载均衡服务，需要用HTTP或者HTTPS协议，用于覆盖服务端的访问地址或域名
     */
    private String loadBalanceServer;
    /**
     * 多网卡配置IP地址前缀
     */
    private String ipPrefix;

    private int port;
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

    private VertxHttpServer vertxHttpServer;

    private RegisterManager registerManager;

    private ProviderDispatcher providerDispatcher;

    private ConsumerDispatcher consumerDispatcher;

    private ProviderWatchDog providerWatchDog;

    private List<RpcFilter> rpcFilters;

    private List<ProviderRouter> providerRouters = new ArrayList<>();

    private List<ConsumerConfig> consumerConfigs = new ArrayList<>();

    private List<ProviderConfig> providerConfigs = new ArrayList<>();

    private WinterRpcBootstrap(String name) {
        this.name = name;
    }

    public static WinterRpcBootstrap instance() {
        return new WinterRpcBootstrap("default");
    }

    public static WinterRpcBootstrap instance(String name) {
        return new WinterRpcBootstrap(name);
    }

    public void start() {
        Objects.requireNonNull(vertxHttpServer);
        Objects.requireNonNull(registerManager);
        Objects.requireNonNull(providerDispatcher);
        Objects.requireNonNull(consumerDispatcher);

        // 持有实例，不能重复
        INSTANCE_MAP.compute(name, (s, winterRpcBootstrap) -> {
            if (winterRpcBootstrap != null) {
                throw new UnsupportedOperationException("duplicate name");
            }
            return this;
        });

        // 先启动监听服务
        vertxHttpServer.startHttpServer(port);

        // 真正注册
        // 启动服务注册者的心跳WatchDog
        providerWatchDog.start();
    }

    public String getHttpProtocol() {
        return httpsEnable ? "https" : "http";
    }

    public String getBindHost() {
        return IpAddressUtil.getLocalIpv4ByNetCard(ipPrefix);
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(StringUtils.trimToNull(name));
    }

    public void setHttpsEnable(boolean httpsEnable) {
        this.httpsEnable = httpsEnable;
    }

    public void setLoadBalanceServer(String loadBalanceServer) {
        this.loadBalanceServer = StringUtils.trimToNull(loadBalanceServer);
    }

    public void setIpPrefix(String ipPrefix) {
        this.ipPrefix = StringUtils.trimToNull(ipPrefix);
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setDefaultSerializerType(String defaultSerializerType) {
        this.defaultSerializerType = defaultSerializerType;
    }

    public void setProviderSerializerSupports(String providerSerializerSupports) {
        this.providerSerializerSupports = providerSerializerSupports;
    }

    public void setConsumerSerializerSupports(String consumerSerializerSupports) {
        this.consumerSerializerSupports = consumerSerializerSupports;
    }

    public void setVertxHttpServer(VertxHttpServer vertxHttpServer) {
        this.vertxHttpServer = vertxHttpServer;
        this.vertxHttpServer.setWinterRpcBootstrap(this);
    }

    public void setRegisterManager(RegisterManager registerManager) {
        this.registerManager = registerManager;
        this.registerManager.setWinterRpcBootstrap(this);
    }

    public void setProviderDispatcher(ProviderDispatcher providerDispatcher) {
        this.providerDispatcher = providerDispatcher;
        this.providerDispatcher.setWinterRpcBootstrap(this);
    }

    public void setConsumerDispatcher(ConsumerDispatcher consumerDispatcher) {
        this.consumerDispatcher = consumerDispatcher;
        this.consumerDispatcher.setWinterRpcBootstrap(this);
    }

    public void setProviderWatchDog(ProviderWatchDog providerWatchDog) {
        this.providerWatchDog = providerWatchDog;
        this.providerWatchDog.setWinterRpcBootstrap(this);
    }

    public void setRpcFilters(List<RpcFilter> rpcFilters) {
        this.rpcFilters = rpcFilters;
        if (this.rpcFilters != null) {
            for (RpcFilter rpcFilter : this.rpcFilters) {
                rpcFilter.setWinterRpcBootstrap(this);
            }
            // 优先级排序
            this.rpcFilters.sort(RpcFilter::compareTo);
        }
    }

    public void setProviderRouters(List<ProviderRouter> providerRouters) {
        this.providerRouters = providerRouters;
        if (this.providerRouters != null) {
            for (ProviderRouter providerRouter : this.providerRouters) {
                providerRouter.setWinterRpcBootstrap(this);
            }
        }
    }

    public void setConsumerConfigs(List<ConsumerConfig> consumerConfigs) {
        this.consumerConfigs = consumerConfigs;
    }

    public void setProviderConfigs(List<ProviderConfig> providerConfigs) {
        this.providerConfigs = providerConfigs;
    }

    public WinterRpcBootstrap name(String name) {
        this.name = name;
        return this;
    }

    public WinterRpcBootstrap vertxHttpServer(VertxHttpServer vertxHttpServer) {
        this.setVertxHttpServer(vertxHttpServer);
        return this;
    }

    public WinterRpcBootstrap registerManager(RegisterManager registerManager) {
        this.setRegisterManager(registerManager);
        return this;
    }

    public WinterRpcBootstrap providerDispatcher(ProviderDispatcher providerDispatcher) {
        this.setProviderDispatcher(providerDispatcher);
        return this;
    }

    public WinterRpcBootstrap consumerDispatcher(ConsumerDispatcher consumerDispatcher) {
        this.setConsumerDispatcher(consumerDispatcher);
        return this;
    }


    public WinterRpcBootstrap providerWatchDog(ProviderWatchDog providerWatchDog) {
        this.setProviderWatchDog(providerWatchDog);
        return this;
    }

    public WinterRpcBootstrap rpcFilter(RpcFilter rpcFilter) {
        this.rpcFilters.add(rpcFilter);
        this.setRpcFilters(rpcFilters);
        return this;
    }

    public WinterRpcBootstrap rpcFilters(List<RpcFilter> rpcFilters) {
        this.setRpcFilters(rpcFilters);
        return this;
    }

    public WinterRpcBootstrap providerRouter(ProviderRouter providerRouter) {
        this.providerRouters.add(providerRouter);
        this.setProviderRouters(providerRouters);
        return this;
    }

    public WinterRpcBootstrap providerRouters(List<ProviderRouter> providerRouters) {
        this.setProviderRouters(providerRouters);
        return this;
    }

    public WinterRpcBootstrap httpsEnable(boolean httpsEnable) {
        this.setHttpsEnable(httpsEnable);
        return this;
    }

    public WinterRpcBootstrap loadBalanceServer(String loadBalanceServer) {
        this.setLoadBalanceServer(loadBalanceServer);
        return this;
    }

    public WinterRpcBootstrap ipPrefix(String ipPrefix) {
        this.setIpPrefix(ipPrefix);
        return this;
    }

    public WinterRpcBootstrap port(int port) {
        this.setPort(port);
        return this;
    }

    public WinterRpcBootstrap defaultSerializerType(String defaultSerializerType) {
        this.setDefaultSerializerType(defaultSerializerType);
        return this;
    }

    public WinterRpcBootstrap providerSerializerSupports(String providerSerializerSupports) {
        this.setProviderSerializerSupports(providerSerializerSupports);
        return this;
    }

    public WinterRpcBootstrap consumerSerializerSupports(String consumerSerializerSupports) {
        this.setConsumerSerializerSupports(consumerSerializerSupports);
        return this;
    }

    public WinterRpcBootstrap consumerConfig(ConsumerConfig consumerConfig) {
        this.consumerConfigs.add(Objects.requireNonNull(consumerConfig));
        return this;
    }

    public WinterRpcBootstrap consumerConfigList(List<ConsumerConfig> consumerConfigList) {
        this.setConsumerConfigs(consumerConfigList);
        return this;
    }

    public WinterRpcBootstrap providerConfig(ProviderConfig providerConfig) {
        this.providerConfigs.add(Objects.requireNonNull(providerConfig));
        return this;
    }

    public WinterRpcBootstrap providerConfigList(List<ProviderConfig> providerConfigList) {
        this.setProviderConfigs(providerConfigList);
        return this;
    }
}

package tech.obiteaaron.winter.embed.rpc;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import tech.obiteaaron.winter.embed.rpc.async.AsyncHelper;
import tech.obiteaaron.winter.embed.rpc.constant.IpAddressUtil;
import tech.obiteaaron.winter.embed.rpc.executing.ConsumerDispatcher;
import tech.obiteaaron.winter.embed.rpc.executing.ProviderDispatcher;
import tech.obiteaaron.winter.embed.rpc.filter.RpcFilter;
import tech.obiteaaron.winter.embed.rpc.regesiter.ConsumerConfig;
import tech.obiteaaron.winter.embed.rpc.regesiter.ProviderConfig;
import tech.obiteaaron.winter.embed.rpc.regesiter.RegisterManager;
import tech.obiteaaron.winter.embed.rpc.router.ProviderRouter;
import tech.obiteaaron.winter.embed.rpc.scheduler.ProviderWatchDog;
import tech.obiteaaron.winter.embed.rpc.server.HttpServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 支持多实例，各用各的，多实例之间相互隔离，互不影响，各注册各的服务提供者和消费者。
 * 初始化方法可以参考适配Spring的自动配置类。
 */
@Getter
public class WinterRpcBootstrap {

    public static final ConcurrentHashMap<String, WinterRpcBootstrap> INSTANCE_MAP = new ConcurrentHashMap<>();
    /**
     * 多实例唯一名称，非ApplicationName
     */
    private String name;
    /**
     * 配置属性
     */
    private WinterRpcConfig winterRpcConfig;
    /**
     * 服务提供者默认支持的序列化类型，多个用逗号“,”隔开
     */
    private String providerSerializerSupports = "hessian,json";

    /**
     * 服务消费者默认支持的序列化类型，多个用逗号“,”隔开
     */
    private String consumerSerializerSupports = "hessian,json";

    private HttpServer httpServer;

    private RegisterManager registerManager;

    private ProviderDispatcher providerDispatcher;

    private ConsumerDispatcher consumerDispatcher;

    private ProviderWatchDog providerWatchDog;

    private List<RpcFilter> rpcFilters;

    private List<ProviderRouter> providerRouters = new ArrayList<>();

    private List<ConsumerConfig> consumerConfigs = new ArrayList<>();

    private List<ProviderConfig> providerConfigs = new ArrayList<>();

    private AsyncHelper asyncHelper;

    private WinterRpcBootstrap(String name) {
        this.name = name;
    }

    public static WinterRpcBootstrap instance() {
        return new WinterRpcBootstrap("default");
    }

    public static WinterRpcBootstrap instance(String name) {
        return new WinterRpcBootstrap(name);
    }

    private final AtomicBoolean initialized = new AtomicBoolean();

    public void start() {
        if (!initialized.compareAndSet(false, true)) {
            return;
        }
        Objects.requireNonNull(httpServer);
        Objects.requireNonNull(registerManager);
        Objects.requireNonNull(providerDispatcher);
        Objects.requireNonNull(consumerDispatcher);
        Objects.requireNonNull(winterRpcConfig);

        // 持有实例，不能重复
        INSTANCE_MAP.compute(name, (s, winterRpcBootstrap) -> {
            if (winterRpcBootstrap != null) {
                throw new UnsupportedOperationException("duplicate name");
            }
            return this;
        });

        // 先启动监听服务
        httpServer.startHttpServer(winterRpcConfig.getPort(),
                winterRpcConfig.getProviderThreadPoolSize(),
                winterRpcConfig.isUseVirtualThread());

        // 真正注册
        // 启动服务注册者的心跳WatchDog
        providerWatchDog.start();
    }

    public String getHttpProtocol() {
        return winterRpcConfig.isHttpsEnable() ? "https" : "http";
    }

    public String getBindHost() {
        return IpAddressUtil.getLocalIpv4ByNetCard(winterRpcConfig.getIpPrefix());
    }

    public WinterRpcBootstrap setName(String name) {
        this.name = Objects.requireNonNull(StringUtils.trimToNull(name));
        return this;
    }

    public WinterRpcBootstrap setWinterRpcConfig(WinterRpcConfig winterRpcConfig) {
        this.winterRpcConfig = winterRpcConfig;
        return this;
    }

    public WinterRpcBootstrap setProviderSerializerSupports(String providerSerializerSupports) {
        this.providerSerializerSupports = providerSerializerSupports;
        return this;
    }

    public WinterRpcBootstrap setConsumerSerializerSupports(String consumerSerializerSupports) {
        this.consumerSerializerSupports = consumerSerializerSupports;
        return this;
    }

    public WinterRpcBootstrap setHttpServer(HttpServer httpServer) {
        this.httpServer = httpServer;
        this.httpServer.setWinterRpcBootstrap(this);
        return this;
    }

    public WinterRpcBootstrap setRegisterManager(RegisterManager registerManager) {
        this.registerManager = registerManager;
        this.registerManager.setWinterRpcBootstrap(this);
        return this;
    }

    public WinterRpcBootstrap setProviderDispatcher(ProviderDispatcher providerDispatcher) {
        this.providerDispatcher = providerDispatcher;
        this.providerDispatcher.setWinterRpcBootstrap(this);
        return this;
    }

    public WinterRpcBootstrap setConsumerDispatcher(ConsumerDispatcher consumerDispatcher) {
        this.consumerDispatcher = consumerDispatcher;
        this.consumerDispatcher.setWinterRpcBootstrap(this);
        return this;
    }

    public WinterRpcBootstrap setProviderWatchDog(ProviderWatchDog providerWatchDog) {
        this.providerWatchDog = providerWatchDog;
        this.providerWatchDog.setWinterRpcBootstrap(this);
        return this;
    }

    public WinterRpcBootstrap setRpcFilters(List<RpcFilter> rpcFilters) {
        this.rpcFilters = rpcFilters;
        if (this.rpcFilters != null) {
            for (RpcFilter rpcFilter : this.rpcFilters) {
                rpcFilter.setWinterRpcBootstrap(this);
            }
            // 优先级排序
            this.rpcFilters.sort(RpcFilter::compareTo);
        }
        return this;
    }

    public WinterRpcBootstrap addRpcFilter(RpcFilter rpcFilter) {
        this.rpcFilters.add(rpcFilter);
        this.setRpcFilters(rpcFilters);
        return this;
    }

    public WinterRpcBootstrap setProviderRouters(List<ProviderRouter> providerRouters) {
        this.providerRouters = providerRouters;
        if (this.providerRouters != null) {
            for (ProviderRouter providerRouter : this.providerRouters) {
                providerRouter.setWinterRpcBootstrap(this);
            }
            // 优先级排序
            this.providerRouters.sort(ProviderRouter::compareTo);
        }
        return this;
    }

    public WinterRpcBootstrap addProviderRouter(ProviderRouter providerRouter) {
        this.providerRouters.add(providerRouter);
        this.setProviderRouters(providerRouters);
        return this;
    }

    public WinterRpcBootstrap setConsumerConfigs(List<ConsumerConfig> consumerConfigs) {
        this.consumerConfigs = consumerConfigs;
        return this;
    }

    public WinterRpcBootstrap addConsumerConfig(ConsumerConfig consumerConfig) {
        this.consumerConfigs.add(Objects.requireNonNull(consumerConfig));
        return this;
    }

    public WinterRpcBootstrap setProviderConfigs(List<ProviderConfig> providerConfigs) {
        this.providerConfigs = providerConfigs;
        return this;
    }

    public WinterRpcBootstrap addProviderConfig(ProviderConfig providerConfig) {
        this.providerConfigs.add(Objects.requireNonNull(providerConfig));
        return this;
    }

    public WinterRpcBootstrap setAsyncHelper(AsyncHelper asyncHelper) {
        this.asyncHelper = asyncHelper;
        return this;
    }

    public String getSerializerType() {
        return winterRpcConfig.getSerializerType();
    }

    public String getApplicationName() {
        return winterRpcConfig.getApplicationName();
    }

    public String getLoadBalanceServer() {
        return winterRpcConfig.getLoadBalanceServer();
    }

    public int getPort() {
        return winterRpcConfig.getPort();
    }

    public boolean isLogging() {
        return winterRpcConfig.isLogging();
    }
}

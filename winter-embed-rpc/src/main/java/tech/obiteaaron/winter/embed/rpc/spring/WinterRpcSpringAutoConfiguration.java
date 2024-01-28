package tech.obiteaaron.winter.embed.rpc.spring;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SmartApplicationListener;
import tech.obiteaaron.winter.common.tools.http.CommonOkHttpClient;
import tech.obiteaaron.winter.common.tools.http.OkHttpClientFactory;
import tech.obiteaaron.winter.embed.registercenter.RegisterService;
import tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap;
import tech.obiteaaron.winter.embed.rpc.WinterRpcConfig;
import tech.obiteaaron.winter.embed.rpc.executing.ConsumerDispatcher;
import tech.obiteaaron.winter.embed.rpc.executing.ProviderDispatcher;
import tech.obiteaaron.winter.embed.rpc.executing.impl.ConsumerDispatcherImpl;
import tech.obiteaaron.winter.embed.rpc.executing.impl.ProviderDispatcherImpl;
import tech.obiteaaron.winter.embed.rpc.filter.LoggingRpcFilter;
import tech.obiteaaron.winter.embed.rpc.filter.MonitorRpcFilter;
import tech.obiteaaron.winter.embed.rpc.filter.RpcFilter;
import tech.obiteaaron.winter.embed.rpc.filter.TracingRpcFilter;
import tech.obiteaaron.winter.embed.rpc.regesiter.RegisterManager;
import tech.obiteaaron.winter.embed.rpc.regesiter.impl.RegisterManagerImpl;
import tech.obiteaaron.winter.embed.rpc.router.ProviderRouter;
import tech.obiteaaron.winter.embed.rpc.router.RoundRobinProviderRouterImpl;
import tech.obiteaaron.winter.embed.rpc.scheduler.ProviderWatchDog;
import tech.obiteaaron.winter.embed.rpc.server.HttpServer;
import tech.obiteaaron.winter.embed.rpc.server.impl.VertxHttpServerImpl;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@EnableConfigurationProperties(WinterRpcProperties.class)
@Configuration
public class WinterRpcSpringAutoConfiguration implements SmartApplicationListener, ApplicationContextAware {

    ApplicationContext applicationContext;

    private final AtomicBoolean atomicBoolean = new AtomicBoolean();

    private WinterRpcBootstrap winterRpcBootstrap;

    @Bean
    public WinterRpcSpringBeanFactoryPostProcessor winterRpcSpringBeanFactoryPostProcessor() {
        WinterRpcSpringBeanFactoryPostProcessor bean = new WinterRpcSpringBeanFactoryPostProcessor();
        winterRpcBootstrap = WinterRpcBootstrap.instance();
        bean.setWinterRpcBootstrap(winterRpcBootstrap);
        return bean;
    }

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return ContextRefreshedEvent.class.isAssignableFrom(eventType);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (atomicBoolean.compareAndSet(false, true)) {
            // 延迟赋值
            WinterRpcProperties winterRpcProperties = applicationContext.getBean(WinterRpcProperties.class);

            // 这里的Bean，可以从Spring里面获取，如果有，则直接使用，否则new一个
            RegisterService registerService = applicationContext.getBean(RegisterService.class);
            // 扩展支持自定义RegisterManager
            RegisterManager registerManager = getBeanPrimary(RegisterManager.class, RegisterManagerImpl::new);
            registerManager.setRegisterService(registerService);
            // 扩展支持自定义ConsumerDispatcher
            HttpServer httpServer = getBeanPrimary(HttpServer.class, VertxHttpServerImpl::new);
            // 扩展支持自定义ConsumerDispatcher
            ConsumerDispatcher consumerDispatcher = getBeanPrimary(ConsumerDispatcher.class, () -> {
                ConsumerDispatcherImpl bean = new ConsumerDispatcherImpl();
                int consumerThreadPoolSize = winterRpcProperties.getConsumerThreadPoolSize();
                if (consumerThreadPoolSize > 0) {
                    CommonOkHttpClient commonOkHttpClient = new CommonOkHttpClient(OkHttpClientFactory.create(0,
                            winterRpcProperties.getConsumerTimeoutMilliSecond(),
                            consumerThreadPoolSize,
                            consumerThreadPoolSize,
                            false, true, null
                    ));
                    bean.setCommonOkHttpClient(commonOkHttpClient);
                } else {
                    consumerThreadPoolSize = 1;
                    CommonOkHttpClient commonOkHttpClient = new CommonOkHttpClient(OkHttpClientFactory.create(0,
                            winterRpcProperties.getConsumerTimeoutMilliSecond(),
                            consumerThreadPoolSize,
                            consumerThreadPoolSize,
                            false, false, null
                    ));
                    bean.setCommonOkHttpClient(commonOkHttpClient);
                }
                return bean;
            });
            // 扩展支持自定义ProviderDispatcher
            ProviderDispatcher providerDispatcher = getBeanPrimary(ProviderDispatcherImpl.class, ProviderDispatcherImpl::new);
            // 扩展支持自定义ProviderRouter
            ProviderRouter providerRouter = getBeanPrimary(ProviderRouter.class, RoundRobinProviderRouterImpl::new);

            // 扩展支持自定义RpcFilter
            Map<String, RpcFilter> rpcFilterMap = applicationContext.getBeansOfType(RpcFilter.class);
            // 将Spring的配置复制到实例配置里面
            WinterRpcConfig winterRpcConfig = new WinterRpcConfig();
            BeanUtils.copyProperties(winterRpcProperties, winterRpcConfig);
            winterRpcBootstrap.setHttpServer(httpServer)
                    .setWinterRpcConfig(winterRpcConfig)
                    .setRegisterManager(registerManager)
                    .setProviderDispatcher(providerDispatcher)
                    .setConsumerDispatcher(consumerDispatcher)
                    .setProviderWatchDog(new ProviderWatchDog())
                    .addProviderRouter(providerRouter)
                    .setRpcFilters(new ArrayList<>(rpcFilterMap.values()))
                    .addRpcFilter(new LoggingRpcFilter())
                    .addRpcFilter(new TracingRpcFilter())
                    .addRpcFilter(new MonitorRpcFilter());
            // 启动
            winterRpcBootstrap.start();
        }
    }

    private <T> T getBeanPrimary(Class<T> clazz, Supplier<T> supplier) {
        try {
            return applicationContext.getBean(clazz);
        } catch (NoSuchBeanDefinitionException ignore) {
            return supplier.get();
        }
    }

    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return true;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}

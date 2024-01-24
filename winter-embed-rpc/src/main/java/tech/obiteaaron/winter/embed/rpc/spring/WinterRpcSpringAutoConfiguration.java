package tech.obiteaaron.winter.embed.rpc.spring;

import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SmartApplicationListener;
import tech.obiteaaron.winter.common.tools.http.OkHttpClientFactory;
import tech.obiteaaron.winter.embed.registercenter.RegisterService;
import tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap;
import tech.obiteaaron.winter.embed.rpc.executing.ConsumerDispatcher;
import tech.obiteaaron.winter.embed.rpc.executing.ProviderDispatcher;
import tech.obiteaaron.winter.embed.rpc.regesiter.RegisterManager;
import tech.obiteaaron.winter.embed.rpc.router.RoundRobinProviderRouterImpl;
import tech.obiteaaron.winter.embed.rpc.server.VertxHttpServer;

import java.util.concurrent.atomic.AtomicBoolean;

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
            RegisterService registerService = applicationContext.getBean(RegisterService.class);

            RegisterManager registerManager = new RegisterManager();
            registerManager.setRegisterService(registerService);
            ProviderDispatcher providerDispatcher = new ProviderDispatcher();
            ConsumerDispatcher consumerDispatcher = new ConsumerDispatcher();
            consumerDispatcher.setCommonOkHttpClient(OkHttpClientFactory.commonOkHttpClient());
            VertxHttpServer vertxHttpServer = new VertxHttpServer();

            winterRpcBootstrap.vertxHttpServer(vertxHttpServer)
                    .registerManager(registerManager)
                    .providerDispatcher(providerDispatcher)
                    .consumerDispatcher(consumerDispatcher)
                    .defaultSerializerType("json")
                    .providerRouter(new RoundRobinProviderRouterImpl())
                    .ipPrefix(winterRpcProperties.getIpPrefix())
                    .port(winterRpcProperties.getPort())
                    .httpsEnable(winterRpcProperties.isHttpsEnable())
                    .loadBalanceServer(winterRpcProperties.getLoadBalanceServer());
            // 启动
            winterRpcBootstrap.start();
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

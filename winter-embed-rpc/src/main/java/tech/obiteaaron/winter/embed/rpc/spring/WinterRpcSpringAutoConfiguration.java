package tech.obiteaaron.winter.embed.rpc.spring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SmartApplicationListener;
import tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap;
import tech.obiteaaron.winter.embed.rpc.executing.ConsumerDispatcher;
import tech.obiteaaron.winter.embed.rpc.executing.ProviderDispatcher;
import tech.obiteaaron.winter.embed.rpc.regesiter.ProviderConfig;
import tech.obiteaaron.winter.embed.rpc.regesiter.RegisterManager;
import tech.obiteaaron.winter.embed.rpc.server.VertxHttpServer;

import java.util.concurrent.atomic.AtomicBoolean;

@Configuration
public class WinterRpcSpringAutoConfiguration implements SmartApplicationListener {

    @Value("${tech.obiteaaron.winter.embed.rpc.port}")
    private int port = 7080;

    private final AtomicBoolean atomicBoolean = new AtomicBoolean();

    private WinterRpcBootstrap winterRpcBootstrapSpringInstance;

    @Bean
    public WinterRpcSpringBeanFactoryPostProcessor winterRpcSpringBeanFactoryPostProcessor() {
        WinterRpcSpringBeanFactoryPostProcessor bean = new WinterRpcSpringBeanFactoryPostProcessor();
        winterRpcBootstrapSpringInstance = winterRpcBootstrap();
        bean.setWinterRpcBootstrap(winterRpcBootstrapSpringInstance);
        return bean;
    }

    public WinterRpcBootstrap winterRpcBootstrap() {
        // 构造一下对象内的属性
        RegisterManager registerManager = new RegisterManager();
        ProviderDispatcher providerDispatcher = new ProviderDispatcher();
        providerDispatcher.setRegisterManager(registerManager);
        ConsumerDispatcher consumerDispatcher = new ConsumerDispatcher();
        consumerDispatcher.setRegisterManager(registerManager);
        // 避免直接依赖SpringBean，此处不赋值
        registerManager.setRegisterService(null);
        WinterRpcBootstrap winterRpcBootstrap = new WinterRpcBootstrap();
        VertxHttpServer vertxHttpServer = new VertxHttpServer();
        vertxHttpServer.setProviderDispatcher(providerDispatcher);
        winterRpcBootstrap.setVertxHttpServer(vertxHttpServer);
        winterRpcBootstrap.setRegisterManager(registerManager);
        winterRpcBootstrap.setProviderDispatcher(providerDispatcher);
        winterRpcBootstrap.setConsumerDispatcher(consumerDispatcher);

        winterRpcBootstrap.setPort(port);

        return winterRpcBootstrap;
    }

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return ContextRefreshedEvent.class.isAssignableFrom(eventType);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (atomicBoolean.compareAndSet(false, true)) {
            winterRpcBootstrapSpringInstance.start();
            for (ProviderConfig providerConfig : WinterRpcSpringBeanFactoryPostProcessor.providerConfigList) {
                winterRpcBootstrapSpringInstance.getRegisterManager().register(providerConfig);
            }
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
}

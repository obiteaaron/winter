package tech.obiteaaron.winter.embed.rpc.spring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SmartApplicationListener;
import tech.obiteaaron.winter.embed.registercenter.impl.DefaultRegisterServiceImpl;
import tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap;
import tech.obiteaaron.winter.embed.rpc.executing.ProviderDispatcher;
import tech.obiteaaron.winter.embed.rpc.regesiter.RegisterManager;
import tech.obiteaaron.winter.embed.rpc.server.VertxHttpServer;

import java.util.concurrent.atomic.AtomicBoolean;

@Configuration
public class WinterRpcSpringAutoConfiguration implements SmartApplicationListener {
    private final RegisterManager registerManager = new RegisterManager();

    @Value("${tech.obiteaaron.winter.embed.rpc.port}")
    private int port = 7080;

    private final AtomicBoolean atomicBoolean = new AtomicBoolean();

    @Bean
    public WinterRpcSpringBeanFactoryPostProcessor winterRpcSpringBeanFactoryPostProcessor() {
        WinterRpcSpringBeanFactoryPostProcessor bean = new WinterRpcSpringBeanFactoryPostProcessor();
        bean.setRegisterManager(registerManager);
        return bean;
    }

    public WinterRpcBootstrap winterRpcBootstrap() {
        // 构造一下对象内的属性
        ProviderDispatcher providerDispatcher = new ProviderDispatcher();
        providerDispatcher.setRegisterManager(registerManager);
        registerManager.setRegisterService(new DefaultRegisterServiceImpl());
        WinterRpcBootstrap winterRpcBootstrap = new WinterRpcBootstrap();
        VertxHttpServer vertxHttpServer = new VertxHttpServer();
        vertxHttpServer.setProviderDispatcher(providerDispatcher);
        winterRpcBootstrap.setVertxHttpServer(vertxHttpServer);
        winterRpcBootstrap.setRegisterManager(registerManager);
        winterRpcBootstrap.setProviderDispatcher(providerDispatcher);
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
            WinterRpcBootstrap winterRpcBootstrap = winterRpcBootstrap();
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
}

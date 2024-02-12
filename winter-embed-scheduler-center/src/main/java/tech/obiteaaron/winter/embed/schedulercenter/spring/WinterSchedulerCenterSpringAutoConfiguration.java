package tech.obiteaaron.winter.embed.schedulercenter.spring;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SmartApplicationListener;
import tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap;
import tech.obiteaaron.winter.embed.schedulercenter.WinterSchedulerCenter;
import tech.obiteaaron.winter.embed.schedulercenter.WinterSchedulerCenterConfig;
import tech.obiteaaron.winter.embed.schedulercenter.executor.SpringBeanParserImpl;
import tech.obiteaaron.winter.embed.schedulercenter.repository.WinterJobInstanceRepository;
import tech.obiteaaron.winter.embed.schedulercenter.repository.WinterJobInstanceTaskRepository;
import tech.obiteaaron.winter.embed.schedulercenter.repository.WinterJobRepository;
import tech.obiteaaron.winter.embed.schedulercenter.repository.impl.jpa.WinterJobInstanceJpaRepositoryImpl;
import tech.obiteaaron.winter.embed.schedulercenter.repository.impl.jpa.WinterJobJpaRepositoryImpl;
import tech.obiteaaron.winter.embed.schedulercenter.repository.impl.memory.WinterJobInstanceMemoryRepositoryImpl;
import tech.obiteaaron.winter.embed.schedulercenter.repository.impl.memory.WinterJobInstanceTaskMemoryRepositoryImpl;
import tech.obiteaaron.winter.embed.schedulercenter.repository.impl.memory.WinterJobMemoryRepositoryImpl;
import tech.obiteaaron.winter.embed.schedulercenter.repository.impl.mysql.WinterJobInstanceMysqlRepositoryImpl;
import tech.obiteaaron.winter.embed.schedulercenter.repository.impl.mysql.WinterJobMysqlRepositoryImpl;

import javax.sql.DataSource;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@EnableConfigurationProperties(WinterSchedulerCenterProperties.class)
@Configuration
@ConditionalOnProperty(value = "tech.obiteaaron.winter.embed.schedulercenter.enable", havingValue = "true", matchIfMissing = true)
public class WinterSchedulerCenterSpringAutoConfiguration implements SmartApplicationListener, ApplicationContextAware {

    ApplicationContext applicationContext;

    private final AtomicBoolean atomicBoolean = new AtomicBoolean();

    @Bean
    public WinterSchedulerCenterSpringBeanPostProcessor winterSchedulerCenterSpringBeanPostProcessor() {
        return new WinterSchedulerCenterSpringBeanPostProcessor();
    }

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return ContextRefreshedEvent.class.isAssignableFrom(eventType);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (atomicBoolean.compareAndSet(false, true)) {
            WinterJobRepository winterJobRepository = getBeanPrimary(WinterJobRepository.class, WinterJobMemoryRepositoryImpl::new);
            WinterJobInstanceRepository winterJobInstanceRepository = getBeanPrimary(WinterJobInstanceRepository.class, WinterJobInstanceMemoryRepositoryImpl::new);
            WinterJobInstanceTaskRepository winterJobInstanceTaskRepository = getBeanPrimary(WinterJobInstanceTaskRepository.class, WinterJobInstanceTaskMemoryRepositoryImpl::new);


            WinterSchedulerCenterProperties winterSchedulerCenterProperties = getBeanPrimary(WinterSchedulerCenterProperties.class, WinterSchedulerCenterProperties::new);
            WinterSchedulerCenterConfig winterSchedulerCenterConfig = new WinterSchedulerCenterConfig();
            BeanUtils.copyProperties(winterSchedulerCenterProperties, winterSchedulerCenterConfig);

            WinterSchedulerCenter.INSTANCE
                    .setWinterSchedulerCenterConfig(winterSchedulerCenterConfig)
                    .setWinterJobRepository(winterJobRepository)
                    .setWinterJobInstanceRepository(winterJobInstanceRepository)
                    .setWinterJobInstanceTaskRepository(winterJobInstanceTaskRepository)
                    .setBeanParser(new SpringBeanParserImpl());
            if (winterSchedulerCenterProperties.isEnableMapJobClusterRpc() && winterSchedulerCenterProperties.isUseDefaultWinterRpcBootstrap()) {
                WinterRpcBootstrap winterRpcBootstrap = WinterRpcBootstrap.INSTANCE_MAP.get("default");
                WinterSchedulerCenter.INSTANCE.setWinterRpcBootstrap(winterRpcBootstrap);
            }
            // 启动调度中心后台任务
            WinterSchedulerCenter.INSTANCE.start();
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
        return 100;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Configuration
    @ConditionalOnProperty(value = "tech.obiteaaron.winter.embed.schedulercenter.repositoryType", havingValue = "JPA")
    public static class JpaRepository {

        @Bean
        public WinterJobInstanceRepository winterJobInstanceRepository(DataSource dataSource) {
            return new WinterJobInstanceJpaRepositoryImpl();
        }

        @Bean
        public WinterJobRepository winterJobRepository(DataSource dataSource) {
            return new WinterJobJpaRepositoryImpl();
        }

    }

    @Configuration
    @ConditionalOnProperty(value = "tech.obiteaaron.winter.embed.schedulercenter.repositoryType", havingValue = "MYSQL")
    public static class MysqlRepository {

        @Bean
        public WinterJobInstanceRepository winterJobInstanceRepository(DataSource dataSource) {
            return new WinterJobInstanceMysqlRepositoryImpl();
        }

        @Bean
        public WinterJobRepository winterJobRepository(DataSource dataSource) {
            return new WinterJobMysqlRepositoryImpl();
        }
    }
}

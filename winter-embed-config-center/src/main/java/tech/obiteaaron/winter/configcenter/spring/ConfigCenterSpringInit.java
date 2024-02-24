package tech.obiteaaron.winter.configcenter.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.core.Ordered;
import tech.obiteaaron.winter.configcenter.repository.ConfigDatabaseRepository;
import tech.obiteaaron.winter.configcenter.repository.impl.ConfigDatabaseRepositoryMysqlImpl;
import tech.obiteaaron.winter.configcenter.scheduler.ConfigCenterInner;
import tech.obiteaaron.winter.configcenter.service.ConfigManagerService;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
public class ConfigCenterSpringInit implements ApplicationContextAware, SmartApplicationListener {

    private static final AtomicBoolean INITIALIZED = new AtomicBoolean();
    private static ApplicationContext applicationContext;

    /**
     * 注意时机，建议所有应用都在 {@link ContextRefreshedEvent} 事件之后才开始，此时基本可以确保应用正常起来了。
     * 这里是最高优先级，依赖配置项的自动启动项，确保优先级低一点即可。
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ConfigCenterSpringInit.applicationContext = applicationContext;
    }

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return ContextRefreshedEvent.class.isAssignableFrom(eventType);
    }

    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return true;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (!INITIALIZED.compareAndSet(false, true)) {
            log.warn("ConfigCenter already INITIALIZED");
            return;
        }
        List<Object> beans = Arrays.stream(applicationContext.getBeanDefinitionNames()).map(item -> applicationContext.getBean(item)).collect(Collectors.toList());

        DataSource dataSource = findDatasourceBean();
        ConfigDatabaseRepository configDatabaseRepository = new ConfigDatabaseRepositoryMysqlImpl();
        configDatabaseRepository.setDataSource(dataSource);
        // 给管理类赋值
        applicationContext.getBean(ConfigManagerService.class).setConfigDatabaseRepository(configDatabaseRepository);
        // 初始化并启动
        ConfigCenterInner.initAndStart(beans, configDatabaseRepository);
    }

    private DataSource findDatasourceBean() {
        try {
            return applicationContext.getBean(DataSource.class);
        } catch (NoUniqueBeanDefinitionException e) {
            Map<String, DataSource> beansOfType = applicationContext.getBeansOfType(DataSource.class);
            DataSource dataSource = beansOfType.get("dataSource");
            if (dataSource != null) {
                return dataSource;
            }
            return beansOfType.values().iterator().next();
        }
    }
}

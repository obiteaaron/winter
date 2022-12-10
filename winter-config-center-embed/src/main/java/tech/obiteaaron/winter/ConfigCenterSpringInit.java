package tech.obiteaaron.winter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.core.Ordered;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
public class ConfigCenterSpringInit implements ApplicationContextAware, SmartApplicationListener {

    private static final AtomicBoolean INITIALIZED = new AtomicBoolean();
    private static ApplicationContext applicationContext;

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
    public void onApplicationEvent(ApplicationEvent event) {
        if (!INITIALIZED.compareAndSet(false, true)) {
            log.warn("ConfigCenter already INITIALIZED");
            return;
        }
        List<Object> beans = Arrays.stream(applicationContext.getBeanDefinitionNames()).map(item -> applicationContext.getBean(item)).collect(Collectors.toList());

        // TODO
        DataSource dataSource = null;
        ConfigDatabaseRepository configDatabaseRepository = new ConfigDatabaseRepository();
        configDatabaseRepository.setDataSource(dataSource);

        ConfigCenterInner.initAndStart(beans, configDatabaseRepository);
    }
}

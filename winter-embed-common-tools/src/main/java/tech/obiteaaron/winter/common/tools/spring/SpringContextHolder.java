package tech.obiteaaron.winter.common.tools.spring;

import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
public class SpringContextHolder implements ApplicationContextAware {

    public static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextHolder.applicationContext = applicationContext;
    }

    @Configuration
    public static class AutoConfiguration {

        @Bean
        @ConditionalOnMissingBean(SpringContextHolder.class)
        public SpringContextHolder springContextHolder() {
            return new SpringContextHolder();
        }
    }
}

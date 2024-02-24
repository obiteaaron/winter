package tech.obiteaaron.winter.configcenter.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.obiteaaron.winter.configcenter.service.ConfigManagerService;
import tech.obiteaaron.winter.configcenter.service.impl.ConfigManagerServiceImpl;

@Configuration
public class ConfigCenterSpringAutoConfiguration {

    @Bean
    public ConfigCenterSpringInit configCenterSpringInit() {
        return new ConfigCenterSpringInit();
    }

    @Bean
    public ConfigManagerService configManagerService() {
        return new ConfigManagerServiceImpl();
    }
}
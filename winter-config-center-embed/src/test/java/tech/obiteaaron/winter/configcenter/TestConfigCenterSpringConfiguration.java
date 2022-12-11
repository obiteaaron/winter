package tech.obiteaaron.winter.configcenter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfigCenterSpringConfiguration {

    @Bean
    public ConfigCenterSpringInit configCenterSpringInit() {
        return new ConfigCenterSpringInit();
    }

    @Bean
    public ConfigManager configManager() {
        return new ConfigManager();
    }
}
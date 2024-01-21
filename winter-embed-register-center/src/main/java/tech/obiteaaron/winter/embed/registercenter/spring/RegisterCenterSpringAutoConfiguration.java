package tech.obiteaaron.winter.embed.registercenter.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.obiteaaron.winter.configcenter.ConfigManager;
import tech.obiteaaron.winter.embed.registercenter.RegisterService;
import tech.obiteaaron.winter.embed.registercenter.impl.DefaultRegisterServiceImpl;

@Configuration
public class RegisterCenterSpringAutoConfiguration {

    @Bean
    public RegisterService registerService(ConfigManager configManager) {
        return new DefaultRegisterServiceImpl(configManager);
    }
}
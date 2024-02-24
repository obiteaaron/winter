package tech.obiteaaron.winter.embed.registercenter.spring;

import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.obiteaaron.winter.configcenter.service.ConfigManagerService;
import tech.obiteaaron.winter.embed.registercenter.RegisterCenterConfig;
import tech.obiteaaron.winter.embed.registercenter.RegisterService;
import tech.obiteaaron.winter.embed.registercenter.impl.DefaultRegisterServiceImpl;

@EnableConfigurationProperties(RegisterCenterProperties.class)
@Configuration
public class RegisterCenterSpringAutoConfiguration {

    @Bean
    public RegisterService registerService(ConfigManagerService configManagerService, RegisterCenterProperties registerCenterProperties) {
        RegisterCenterConfig registerCenterConfig = new RegisterCenterConfig();
        BeanUtils.copyProperties(registerCenterProperties, registerCenterConfig);
        return new DefaultRegisterServiceImpl(configManagerService, registerCenterConfig);
    }
}
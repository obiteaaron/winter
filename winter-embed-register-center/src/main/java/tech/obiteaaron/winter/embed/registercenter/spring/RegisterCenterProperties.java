package tech.obiteaaron.winter.embed.registercenter.spring;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("tech.obiteaaron.winter.embed.registercenter")
@Getter
@Setter
public class RegisterCenterProperties {

    /**
     * 服务提供者心跳超时时间，毫秒
     */
    private int providerHeartbeatTimeoutMilliSecond = 3000;
}

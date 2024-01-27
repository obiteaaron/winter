package tech.obiteaaron.winter.embed.rpc.spring;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("tech.obiteaaron.winter.embed.rpc")
@Getter
@Setter
public class WinterRpcProperties {

    private String applicationName = "default";

    private int port = 7080;

    private boolean httpsEnable = false;

    private String loadBalanceServer;

    private String ipPrefix;
}

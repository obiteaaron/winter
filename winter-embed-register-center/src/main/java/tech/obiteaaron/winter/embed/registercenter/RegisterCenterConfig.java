package tech.obiteaaron.winter.embed.registercenter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterCenterConfig {

    /**
     * 服务提供者心跳超时时间，毫秒
     */
    private int providerHeartbeatTimeoutMilliSecond = 3000;

    public int parseProviderHeartbeatTimeoutMilliSecond() {
        // 最小3秒，不能再小
        return Math.max(providerHeartbeatTimeoutMilliSecond, 3000);
    }
}

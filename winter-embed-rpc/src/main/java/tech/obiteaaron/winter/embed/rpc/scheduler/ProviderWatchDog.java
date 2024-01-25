package tech.obiteaaron.winter.embed.rpc.scheduler;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap;
import tech.obiteaaron.winter.embed.rpc.regesiter.ConsumerConfig;
import tech.obiteaaron.winter.embed.rpc.regesiter.ProviderConfig;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ProviderWatchDog {

    @Setter
    WinterRpcBootstrap winterRpcBootstrap;

    public void start() {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                // WatchDog真正注册
                for (ProviderConfig providerConfig : winterRpcBootstrap.getProviderConfigs()) {
                    winterRpcBootstrap.getRegisterManager().register(providerConfig);
                }
                for (ConsumerConfig consumerConfig : winterRpcBootstrap.getConsumerConfigs()) {
                    winterRpcBootstrap.getRegisterManager().subscribe(consumerConfig);
                }
            } catch (Throwable t) {
                log.error("ProviderWatchDog Exception", t);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
}

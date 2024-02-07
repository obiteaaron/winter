package tech.obiteaaron.winter.embed.rpc.scheduler;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import tech.obiteaaron.winter.common.tools.system.SystemStatus;
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
                if (!SystemStatus.running) {
                    return;
                }
                // WatchDog真正注册，并且维持心跳
                for (ProviderConfig providerConfig : winterRpcBootstrap.getProviderConfigs()) {
                    providerConfig.setApplicationName(winterRpcBootstrap.getApplicationName());
                    winterRpcBootstrap.getRegisterManager().register(providerConfig);
                }
                for (ConsumerConfig consumerConfig : winterRpcBootstrap.getConsumerConfigs()) {
                    consumerConfig.setApplicationName(winterRpcBootstrap.getApplicationName());
                    winterRpcBootstrap.getRegisterManager().subscribe(consumerConfig);
                }
            } catch (Throwable t) {
                log.error("ProviderWatchDog Exception", t);
            }
        }, 0, 1, TimeUnit.SECONDS);

        // shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                // 关闭心跳线程池
                scheduledExecutorService.shutdown();
                // 阻塞3秒，确保服务已经下线
                try {
                    scheduledExecutorService.awaitTermination(5, TimeUnit.SECONDS);
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                // 停机时解除注册
                for (ProviderConfig providerConfig : winterRpcBootstrap.getProviderConfigs()) {
                    winterRpcBootstrap.getRegisterManager().unregister(providerConfig);
                }
                for (ConsumerConfig consumerConfig : winterRpcBootstrap.getConsumerConfigs()) {
                    winterRpcBootstrap.getRegisterManager().unsubscribe(consumerConfig);
                }
            }
        }));
    }
}

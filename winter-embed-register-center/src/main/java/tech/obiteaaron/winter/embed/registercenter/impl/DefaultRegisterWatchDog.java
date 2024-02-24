package tech.obiteaaron.winter.embed.registercenter.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.obiteaaron.winter.common.tools.lock.Lock;
import tech.obiteaaron.winter.common.tools.lock.Locks;
import tech.obiteaaron.winter.common.tools.system.SystemStatus;
import tech.obiteaaron.winter.common.tools.threadpool.ThreadUtils;
import tech.obiteaaron.winter.configcenter.Config;
import tech.obiteaaron.winter.configcenter.ConfigCenter;
import tech.obiteaaron.winter.configcenter.service.ConfigManagerService;
import tech.obiteaaron.winter.embed.registercenter.RegisterCenterConfig;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 后台线程，用于删除无效的注册URL数据
 */
@Slf4j
@Component
public class DefaultRegisterWatchDog {

    static final DefaultRegisterWatchDog INSTANCE = new DefaultRegisterWatchDog();

    private static final AtomicBoolean ATOMIC_BOOLEAN = new AtomicBoolean();

    private static ConfigManagerService configManagerService;

    void start(ConfigManagerService configManagerService, RegisterCenterConfig registerCenterConfig) {
        if (!ATOMIC_BOOLEAN.compareAndSet(false, true)) {
            return;
        }
        DefaultRegisterWatchDog.configManagerService = configManagerService;
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        ThreadUtils.registerForShutdown(scheduledExecutorService);
        scheduledExecutorService.scheduleAtFixedRate(() -> doWatchDog(registerCenterConfig), 60, 60, TimeUnit.SECONDS);
        log.info("DefaultRegisterWatchDog started");
    }

    private void doWatchDog(RegisterCenterConfig registerCenterConfig) {
        if (!SystemStatus.running) {
            return;
        }
        try (Lock lock = Locks.newRedisLock("DefaultRegisterWatchDog")) {
            if (!lock.tryLock()) {
                // 加锁失败则直接跳出，等待下次重调。只由一个主节点操作，其他节点不操作。
                return;
            }
            doWatchDog0(registerCenterConfig);
        } catch (Throwable t) {
            log.error("DefaultRegisterWatchDog Exception", t);
        }
    }

    private void doWatchDog0(RegisterCenterConfig registerCenterConfig) {
        // 超过10倍心跳时间的情况下才删除，避免网络延迟导致心跳没上报上来，也给providerHeartbeatTimeoutMilliSecond配置项留足一些可配置空间
        long validProviderTime = System.currentTimeMillis() - registerCenterConfig.parseProviderHeartbeatTimeoutMilliSecond() * 10L;
        // 直接从本地查，本地拥有全量数据
        List<Config> allConfigs = ConfigCenter.getAllConfigs();
        List<Config> invalidUrls = allConfigs.stream()
                // 注册中心前缀
                .filter(item -> item.getGroupName().startsWith("register:"))
                .filter(item -> item.getGmtModified() == null || item.getGmtModified().getTime() < validProviderTime)
                .collect(Collectors.toList());
        for (Config invalidUrl : invalidUrls) {
            int delete = configManagerService.delete(invalidUrl);
            if (delete != 1) {
                log.warn("DefaultRegisterWatchDog delete result invalid id={}, name={}, groupName={}, result={}", invalidUrl.getId(), invalidUrl.getName(), invalidUrl.getGroupName(), delete);
            }
        }
    }
}

package tech.obiteaaron.winter.configcenter.scheduler;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import tech.obiteaaron.winter.common.tools.system.SystemStatus;
import tech.obiteaaron.winter.configcenter.Config;
import tech.obiteaaron.winter.configcenter.repository.ConfigDatabaseRepository;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 配置中心拉取配置的任务
 */
@Slf4j
final class ConfigCenterPullTask {
    @Setter
    private ConfigDatabaseRepository configDatabaseRepository;
    /**
     * 最后一次拉取的时间，毫秒，用于增量拉取
     */
    private Date lastPullDate;

    void autoPull() {
        AtomicReference<ScheduledFuture<?>> scheduledFutureRef = new AtomicReference<>();
        Runnable runnable = () -> {
            try {
                if (!SystemStatus.running) {
                    log.info("ConfigCenter system stopped.");
                    scheduledFutureRef.get().cancel(false);
                    return;
                }
                if (log.isDebugEnabled()) {
                    log.debug("ConfigCenter auto pull starting...");
                }
                Date startDate = new Date();
                List<Config> deltaConfigs = null;
                if (lastPullDate == null) {
                    deltaConfigs = configDatabaseRepository.queryAll();
                } else {
                    deltaConfigs = configDatabaseRepository.queryDelta(lastPullDate);
                }
                int effectNum = ConfigCenterInner.mergeDeltaConfigAndTriggerListener(deltaConfigs);
                if (!deltaConfigs.isEmpty() || effectNum > 0) {
                    if (log.isDebugEnabled()) {
                        log.info("ConfigCenter config changed, delta={}, effectNum={}", deltaConfigs.size(), effectNum);
                    }
                }
                lastPullDate = calcNewLastPullDate(startDate);
                if (log.isDebugEnabled()) {
                    log.info("ConfigCenter auto pull finished, lastPullDate={}", lastPullDate);
                }
            } catch (Throwable t) {
                log.error("ConfigCenter auto pull config and trigger listener exception", t);
            }
        };
        // 立即运行一次
        runnable.run();
        // 后台定时拉取，因为注册中心的心跳超时时间定为3秒有效，这里采用1秒拉取一次的方式，避免拉取不到最新的数据。
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        ScheduledFuture<?> scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(runnable, 0, 1, TimeUnit.SECONDS);
        scheduledFutureRef.set(scheduledFuture);
    }

    private Date calcNewLastPullDate(Date startDate) {
        // 按所有配置的最大时间和拉取的开始时间比较
        return ConfigCenterInner.getAllConfigs().stream()
                .peek(item -> {
                    if (item.getGmtModified() == null) {
                        item.setGmtModified(new Date(0));
                    }
                })
                .max(Comparator.comparingLong(item -> item.getGmtModified().getTime()))
                .map(item -> {
                    // 不能超过开始时间
                    Date gmtModified = item.getGmtModified();
                    return gmtModified.after(startDate) ? startDate : gmtModified;
                }).orElse(new Date());
    }
}

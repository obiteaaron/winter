package tech.obiteaaron.winter.configcenter;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
        Runnable runnable = () -> {
            try {
                log.info("ConfigCenter auto pull starting...");
                Date startDate = new Date();
                List<Config> deltaConfigs = null;
                if (lastPullDate == null) {
                    deltaConfigs = configDatabaseRepository.queryAll();
                } else {
                    deltaConfigs = configDatabaseRepository.queryDelta(lastPullDate);
                }
                int effectNum = ConfigCenterInner.mergeDeltaConfigAndTriggerListener(deltaConfigs);
                if (deltaConfigs.size() > 0 || effectNum > 0) {
                    log.info("ConfigCenter config changed, delta={}, effectNum={}", deltaConfigs.size(), effectNum);
                }
                lastPullDate = calcNewLastPullDate(startDate);
                log.info("ConfigCenter auto pull finished, lastPullDate={}", lastPullDate);
            } catch (Throwable t) {
                log.error("ConfigCenter auto pull config and trigger listener exception", t);
            }
        };
        // 立即运行一次
        runnable.run();
        // 后台定时拉取
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleWithFixedDelay(runnable, 5, 5, TimeUnit.SECONDS);
    }

    private Date calcNewLastPullDate(Date startDate) {
        // 按所有配置的最大时间和拉取的开始时间比较
        return ConfigCenterInner.getAllConfigs().stream()
                .peek(item -> {
                    if (item.getLastModified() == null) {
                        item.setLastModified(new Date(0));
                    }
                })
                .max(Comparator.comparingLong(item -> item.getLastModified().getTime()))
                .map(item -> {
                    // 不能超过开始时间
                    Date lastModified = item.getLastModified();
                    return lastModified.after(startDate) ? startDate : lastModified;
                }).orElse(new Date());
    }
}

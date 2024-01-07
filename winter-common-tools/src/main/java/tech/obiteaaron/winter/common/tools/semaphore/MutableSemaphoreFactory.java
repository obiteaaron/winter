package tech.obiteaaron.winter.common.tools.semaphore;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 可变信号量，通常为了限制机器的整体并行度，如调度任务并行度
 */
@Slf4j
public class MutableSemaphoreFactory {

    private static final ConcurrentHashMap<String, MutableSemaphore> CACHE_MAP = new ConcurrentHashMap<>();

    public static MutableSemaphore newMutableSemaphore(String name, Supplier<Integer> permitsSupplier) {
        return CACHE_MAP.compute(name, (k, v) -> {
            if (v == null) {
                return new MutableSemaphore(name, permitsSupplier);
            } else {
                return v;
            }
        });
    }

    static {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                CACHE_MAP.values().forEach(MutableSemaphore::refresh);
            } catch (Throwable t) {
                log.error("refresh Semaphore Exception", t);
            }
        }, 5, 5, TimeUnit.SECONDS);
    }
}

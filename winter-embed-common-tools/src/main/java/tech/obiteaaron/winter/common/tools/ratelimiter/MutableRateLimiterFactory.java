package tech.obiteaaron.winter.common.tools.ratelimiter;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
public class MutableRateLimiterFactory {

    private static final ConcurrentHashMap<String, MutableRateLimiter> CACHE_MAP = new ConcurrentHashMap<>();

    public static MutableRateLimiter newRateLimiter(String name, Supplier<Integer> configValueSupplier) {
        return CACHE_MAP.compute(name, (k, v) -> {
            if (v == null) {
                return new MutableRateLimiter(name, configValueSupplier);
            } else {
                return v;
            }
        });
    }

    static {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                CACHE_MAP.values().forEach(MutableRateLimiter::refresh);
            } catch (Throwable t) {
                log.error("refresh RateLimiter Exception", t);
            }
        }, 5, 5, TimeUnit.SECONDS);
    }
}

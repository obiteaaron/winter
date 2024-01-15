package tech.obiteaaron.winter.common.tools.ratelimiter;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 用于按配置动态调整限流，具体实现使用Guava的限流
 */
@Slf4j
@SuppressWarnings("UnstableApiUsage")
public class MutableRateLimiter {

    private static final double MIN_RATE = 0.01;

    private final String name;

    private final Supplier<Integer> configValueSupplier;

    private volatile int currentValue;

    private volatile RateLimiter rateLimiter;

    MutableRateLimiter(String name, Supplier<Integer> configValueSupplier) {
        this.name = name;
        this.configValueSupplier = configValueSupplier;
        Objects.requireNonNull(name);
        Objects.requireNonNull(configValueSupplier);
        Objects.requireNonNull(configValueSupplier.get());
        int configValue = configValueSupplier.get();
        if (configValue < 1) {
            this.currentValue = -1;
            this.rateLimiter = RateLimiter.create(MIN_RATE);
        } else {
            this.currentValue = configValue;
            this.rateLimiter = RateLimiter.create(currentValue);
        }
    }

    public RateLimiter get() {
        return rateLimiter;
    }

    void refresh() {
        int oldCurrentValue = this.currentValue;
        Integer newConfigValue = Optional.of(this.configValueSupplier).map(Supplier::get).orElse(1);
        if (newConfigValue < 1) {
            this.currentValue = -1;
        } else {
            this.currentValue = newConfigValue;
        }
        if (oldCurrentValue == currentValue) {
            return;
        }
        log.info("MutableRateLimiter Change name = {}, value = {} --> {}", name, oldCurrentValue, currentValue);
        if (currentValue == -1) {
            this.rateLimiter = RateLimiter.create(MIN_RATE);
        } else {
            this.rateLimiter = RateLimiter.create(currentValue);
        }
    }
}

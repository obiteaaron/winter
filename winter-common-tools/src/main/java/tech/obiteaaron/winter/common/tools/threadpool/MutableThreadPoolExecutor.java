package tech.obiteaaron.winter.common.tools.threadpool;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Supplier;

@Slf4j
public class MutableThreadPoolExecutor extends ThreadPoolExecutor {

    @Getter
    private final String name;

    private final Supplier<Integer> poolSizeSupplier;

    public MutableThreadPoolExecutor(String name, Supplier<Integer> poolSizeSupplier, int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit, @NotNull BlockingQueue<Runnable> workQueue, @NotNull ThreadFactory threadFactory, @NotNull RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        Objects.requireNonNull(name);
        Objects.requireNonNull(poolSizeSupplier);
        Objects.requireNonNull(poolSizeSupplier.get());
        this.name = name;
        this.poolSizeSupplier = poolSizeSupplier;
    }


    public void refresh() {
        Integer newPoolSize = Optional.ofNullable(poolSizeSupplier.get()).orElse(0);
        if (newPoolSize <= 0) {
            return;
        }

        if (newPoolSize == getMaximumPoolSize()) {
            return;
        }
        log.info("MutableThreadPoolExecutor Change name = {}, value = {} --> {}", name, newPoolSize, getMaximumPoolSize());
        if (newPoolSize < getCorePoolSize()) {
            // 如果核心线程数超过了线程池的最大数量，则先减少核心的数量
            setCorePoolSize(newPoolSize);
        }
        setMaximumPoolSize(newPoolSize);
    }
}

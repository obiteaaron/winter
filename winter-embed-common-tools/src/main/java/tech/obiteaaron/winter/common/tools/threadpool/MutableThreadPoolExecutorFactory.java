package tech.obiteaaron.winter.common.tools.threadpool;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import tech.obiteaaron.winter.common.tools.system.SystemStatus;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * 可变线程池，可持有变量，不会自动释放，用完需要手动关闭
 */
@Slf4j
public class MutableThreadPoolExecutorFactory {
    /**
     * 线程池不像其他对象，没有引用时也不会释放，所以需要在这里通过其他手段，触发释放线程池
     */
    private static final ConcurrentHashMap<String, MutableThreadPoolExecutor> CACHE_MAP = new ConcurrentHashMap<>();

    /**
     * 其他方法后面再加
     *
     * @param name             唯一名称
     * @param poolSizeSupplier
     * @return
     */
    public static MutableThreadPoolExecutor newAbortPool(String name, Supplier<Integer> poolSizeSupplier, int queueSize) {
        return CACHE_MAP.compute(name, (k, v) -> {
            if (v == null) {
                return newPool(name, poolSizeSupplier, queueSize, new AbortPolicy(name));
            } else {
                return v;
            }
        });
    }

    public static MutableThreadPoolExecutor newBlockPool(String name, Supplier<Integer> poolSizeSupplier, int queueSize) {
        return CACHE_MAP.compute(name, (k, v) -> {
            if (v == null) {
                return newPool(name, poolSizeSupplier, queueSize, new BlockPolicy(name));
            } else {
                return v;
            }
        });
    }

    public static MutableThreadPoolExecutor newCallerRunPool(String name, Supplier<Integer> poolSizeSupplier, int queueSize) {
        return CACHE_MAP.compute(name, (k, v) -> {
            if (v == null) {
                return newPool(name, poolSizeSupplier, queueSize, new CallerRunsPolicy(name));
            } else {
                return v;
            }
        });
    }

    public static MutableThreadPoolExecutor newDiscardPool(String name, Supplier<Integer> poolSizeSupplier, int queueSize) {
        return CACHE_MAP.compute(name, (k, v) -> {
            if (v == null) {
                return newPool(name, poolSizeSupplier, queueSize, new DiscardPolicy(name));
            } else {
                return v;
            }
        });
    }

    public static MutableThreadPoolExecutor newDiscardOldestPool(String name, Supplier<Integer> poolSizeSupplier, int queueSize) {
        return CACHE_MAP.compute(name, (k, v) -> {
            if (v == null) {
                return newPool(name, poolSizeSupplier, queueSize, new DiscardOldestPolicy(name));
            } else {
                return v;
            }
        });
    }

    private static MutableThreadPoolExecutor newPool(String name,
                                                     Supplier<Integer> poolSizeSupplier,
                                                     int queueSize,
                                                     @NotNull RejectedExecutionHandler handler) {
        Integer maxPoolSize = poolSizeSupplier.get();
        return new MutableThreadPoolExecutor(name,
                poolSizeSupplier,
                queueSize <= 0 ? 0 : maxPoolSize,
                maxPoolSize,
                10,
                TimeUnit.MINUTES,
                queueSize <= 0 ? new SynchronousQueue<>() : new ArrayBlockingQueue<>(queueSize),
                Executors.defaultThreadFactory(),
                handler);
    }

    /**
     * 需要主动关闭线程池
     *
     * @param name 唯一名称
     */
    public static void shutdown(String name) {
        MutableThreadPoolExecutor mutableThreadPoolExecutor = CACHE_MAP.remove(name);
        if (mutableThreadPoolExecutor != null) {
            mutableThreadPoolExecutor.shutdown();
        }
    }

    private static class AbortPolicy extends ThreadPoolExecutor.AbortPolicy {

        private final String poolName;

        private final AtomicInteger triggerTimes = new AtomicInteger();

        public AbortPolicy(String poolName) {
            this.poolName = poolName;
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            log.info("TriggerThreadPoolRejectedPolicy Abort poolName={}, triggerTimes={}", poolName, triggerTimes.incrementAndGet());
            super.rejectedExecution(r, e);
        }
    }

    private static class DiscardPolicy extends ThreadPoolExecutor.DiscardPolicy {

        private final String poolName;

        private final AtomicInteger triggerTimes = new AtomicInteger();

        public DiscardPolicy(String poolName) {
            this.poolName = poolName;
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            log.info("TriggerThreadPoolRejectedPolicy Discard poolName={}, triggerTimes={}", poolName, triggerTimes.incrementAndGet());
            super.rejectedExecution(r, e);
        }
    }

    private static class DiscardOldestPolicy extends ThreadPoolExecutor.DiscardOldestPolicy {

        private final String poolName;

        private final AtomicInteger triggerTimes = new AtomicInteger();

        public DiscardOldestPolicy(String poolName) {
            this.poolName = poolName;
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            log.info("TriggerThreadPoolRejectedPolicy DiscardOldest poolName={}, triggerTimes={}", poolName, triggerTimes.incrementAndGet());
            super.rejectedExecution(r, e);
        }
    }

    private static class CallerRunsPolicy extends ThreadPoolExecutor.CallerRunsPolicy {

        private final String poolName;

        private final AtomicInteger triggerTimes = new AtomicInteger();

        public CallerRunsPolicy(String poolName) {
            this.poolName = poolName;
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            log.info("TriggerThreadPoolRejectedPolicy CallerRuns poolName={}, triggerTimes={}", poolName, triggerTimes.incrementAndGet());
            super.rejectedExecution(r, e);
        }
    }

    private static class BlockPolicy implements RejectedExecutionHandler {

        private final String poolName;

        private final AtomicInteger triggerTimes = new AtomicInteger();

        public BlockPolicy(String poolName) {
            this.poolName = poolName;
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            log.info("TriggerThreadPoolRejectedPolicy BlockPolicy poolName={}, triggerTimes={}", poolName, triggerTimes.incrementAndGet());
            try {
                boolean offer = false;
                while (!offer && SystemStatus.running) {
                    offer = e.getQueue().offer(r, 10, TimeUnit.MILLISECONDS);
                }
            } catch (InterruptedException ex) {
                log.error("TriggerThreadPoolRejectedPolicy BlockPolicy Exception poolName={}", poolName, ex);

            }
        }
    }

    static {
        // 优雅停机
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                CACHE_MAP.values().forEach(ThreadPoolExecutor::shutdown);
                CACHE_MAP.values().forEach(item -> {
                    try {
                        boolean b = item.awaitTermination(2, TimeUnit.MINUTES);
                        if (b) {
                            log.info("Graceful stop success poolName={}", item.getName());
                        } else {
                            log.info("Graceful stop failed poolName={}", item.getName());
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }));
        // watchdog
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                CACHE_MAP.values().forEach(MutableThreadPoolExecutor::refresh);
            } catch (Throwable t) {
                log.error("refresh rateLimiter Exception", t);
            }
        }, 5, 5, TimeUnit.SECONDS);
    }
}

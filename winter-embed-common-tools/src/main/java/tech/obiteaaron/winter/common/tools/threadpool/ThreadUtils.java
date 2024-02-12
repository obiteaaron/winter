package tech.obiteaaron.winter.common.tools.threadpool;

import lombok.extern.slf4j.Slf4j;
import tech.obiteaaron.winter.common.tools.trace.Slf4jMdcUtils;
import tech.obiteaaron.winter.common.tools.trace.TraceUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ThreadUtils {

    public static Runnable wrapperForSubThread(Runnable runnable) {
        String traceId = TraceUtils.getTraceId();
        return () -> {
            try {
                Slf4jMdcUtils.appendMdcForTrace(traceId);
                runnable.run();
            } finally {
                Slf4jMdcUtils.clearMdcComplete();
            }
        };
    }

    public static <V> Callable<V> wrapperForSubThread(Callable<V> callable) {
        String traceId = TraceUtils.getTraceId();
        return () -> {
            try {
                Slf4jMdcUtils.appendMdcForTrace(traceId);
                return callable.call();
            } finally {
                Slf4jMdcUtils.clearMdcComplete();
            }
        };
    }

    public static Runnable wrapperForNoThrowable(Runnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Throwable e) {
                log.error("subThread exception", e);
            }
        };
    }


    private static final ConcurrentHashMap<ExecutorService, ExecutorService> CACHE_EXECUTOR_SERVICE = new ConcurrentHashMap<>();

    public static void registerForShutdown(ExecutorService executorService) {
        CACHE_EXECUTOR_SERVICE.put(executorService, executorService);
    }

    static {
        // 优雅停机
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                CACHE_EXECUTOR_SERVICE.values().forEach(ExecutorService::shutdown);
                CACHE_EXECUTOR_SERVICE.values().forEach(item -> {
                    try {
                        boolean b = item.awaitTermination(2, TimeUnit.MINUTES);
                        if (b) {
                            log.info("Graceful stop success poolName={}", item);
                        } else {
                            log.info("Graceful stop failed poolName={}", item);
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }));
    }
}

package tech.obiteaaron.winter.common.tools.threadpool;

import lombok.extern.slf4j.Slf4j;
import tech.obiteaaron.winter.common.tools.trace.Slf4jMdcUtil;
import tech.obiteaaron.winter.common.tools.trace.TraceUtil;

import java.util.concurrent.Callable;

@Slf4j
public class ThreadUtil {

    public static Runnable wrapperForSubThread(Runnable runnable) {
        String traceId = TraceUtil.getTraceId();
        return () -> {
            try {
                Slf4jMdcUtil.appendMdcForTrace(traceId);
                runnable.run();
            } finally {
                Slf4jMdcUtil.clearMdcComplete();
            }
        };
    }

    public static <V> Callable<V> wrapperForSubThread(Callable<V> callable) {
        String traceId = TraceUtil.getTraceId();
        return () -> {
            try {
                Slf4jMdcUtil.appendMdcForTrace(traceId);
                return callable.call();
            } finally {
                Slf4jMdcUtil.clearMdcComplete();
            }
        };
    }

    public static Runnable wrapperForNoThrowable(Runnable runnable) {
        String traceId = TraceUtil.getTraceId();
        return () -> {
            try {
                runnable.run();
            } catch (Throwable e) {
                log.error("subThread exception", e);
            }
        };
    }
}

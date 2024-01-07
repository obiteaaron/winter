package tech.obiteaaron.winter.common.tools.trace;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class Slf4jMdcUtil {

    private static final ThreadLocal<AtomicInteger> ENTRY_TIMES = ThreadLocal.withInitial(AtomicInteger::new);

    public static void appendMdcForNew() {
        appendMdc(null, true);
    }

    public static void appendMdcForTrace(String traceId) {
        appendMdc(traceId, false);
    }

    private static void appendMdc(String traceId, boolean isNew) {
        try {
            if (isNew) {
                clearMdcComplete();
                traceId = null;
            }
            if (ENTRY_TIMES.get().getAndIncrement() > 0) {
                // 同线程重入的直接跳过
                return;
            }
            String newTraceId = TraceUtil.startTrace(traceId);
            MDC.put("TRACE_ID", newTraceId);
        } catch (Throwable t) {
            log.error("appendMdc Exception", t);
        }
    }

    public static void clearMdc() {
        try {
            if (ENTRY_TIMES.get().decrementAndGet() > 0) {
                // 同线程重入退出未完成则跳过
                return;
            }
            clearMdcComplete();
        } catch (Throwable t) {
            log.error("clearMdc Exception", t);
        }
    }

    public static void clearMdcComplete() {
        try {
            ENTRY_TIMES.remove();
            MDC.remove("TRACE_ID");
        } catch (Throwable t) {
            log.error("clearMdcComplete Exception", t);
        }
    }
}

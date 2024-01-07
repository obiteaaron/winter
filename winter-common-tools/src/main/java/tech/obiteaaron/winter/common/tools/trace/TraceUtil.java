package tech.obiteaaron.winter.common.tools.trace;

import tech.obiteaaron.winter.common.tools.id.UuidGenerator;

public class TraceUtil {

    private static final LocalTrace localTrace = new LocalTrace();

    public static String getTraceId() {
        return localTrace.getTrace();
    }

    public static String startTrace(String traceId) {
        return localTrace.startTrace(traceId);
    }

    public static void stopTrace() {
        localTrace.stopTrace();
    }

    public interface Trace {

        String getTrace();

        String startTrace(String traceId);

        void stopTrace();
    }

    public static class LocalTrace implements Trace {

        private static final ThreadLocal<String> TRACE_THREAD_LOCAL = new ThreadLocal<>();

        @Override
        public String getTrace() {
            return TRACE_THREAD_LOCAL.get();
        }

        @Override
        public String startTrace(String traceId) {
            String newTraceId = traceId != null ? traceId : UuidGenerator.generate();
            TRACE_THREAD_LOCAL.set(traceId);
            return newTraceId;
        }

        @Override
        public void stopTrace() {
            TRACE_THREAD_LOCAL.remove();
        }
    }
}

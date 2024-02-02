package tech.obiteaaron.winter.embed.rpc.filter;

import com.google.common.collect.Lists;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import tech.obiteaaron.winter.embed.registercenter.model.URL;
import tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap;
import tech.obiteaaron.winter.embed.rpc.constant.InvokerStage;
import tech.obiteaaron.winter.embed.rpc.executing.InvokeContext;
import tech.obiteaaron.winter.embed.rpc.filter.chain.FilterChain;

import java.util.List;

@Slf4j
public class TracingRpcFilter implements RpcFilter {

    private static final String TRACE_ID = "TRACE_ID";

    @Setter
    WinterRpcBootstrap winterRpcBootstrap;

    @Override
    public List<String> supportStageList() {
        return Lists.newArrayList(InvokerStage.CONSUMER.name(), InvokerStage.PROVIDER.name());
    }

    @Override
    public void invoke(String invokeStage, URL url, InvokeContext context, FilterChain filterChain) {
        try {
            if (InvokerStage.CONSUMER.name().equals(invokeStage)) {
                String traceId = MDC.get(TRACE_ID);
                context.setTraceId(traceId);
            } else {
                String traceId = context.getTraceId();
                MDC.put(TRACE_ID, traceId);
            }
            filterChain.invoke(invokeStage, url, context);
        } finally {
            // 这里不是子线程，没有做隔离，所以暂时不需要清理MDC的traceId信息
        }
    }

    @Override
    public int order() {
        return Integer.MIN_VALUE + 10000;
    }
}

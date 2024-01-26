package tech.obiteaaron.winter.embed.rpc.filter;

import com.google.common.collect.Lists;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import tech.obiteaaron.winter.common.tools.json.JsonUtil;
import tech.obiteaaron.winter.embed.registercenter.model.URL;
import tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap;
import tech.obiteaaron.winter.embed.rpc.constant.InvokerStage;
import tech.obiteaaron.winter.embed.rpc.executing.InvokeContext;
import tech.obiteaaron.winter.embed.rpc.filter.chain.FilterChain;

import java.util.List;

@Slf4j
public class MonitorRpcFilter implements RpcFilter {

    @Setter
    WinterRpcBootstrap winterRpcBootstrap;

    @Override
    public List<String> supportStageList() {
        return Lists.newArrayList(InvokerStage.CONSUMER.name(), InvokerStage.PROVIDER.name());
    }

    @Override
    public void invoke(String invokeStage, URL url, InvokeContext context, FilterChain filterChain) {
        try {
            log.info("beforeInvoke invokeState={}, url={}, context={}", invokeStage, url, JsonUtil.toJsonString(context));
            filterChain.invoke(invokeStage, url, context);
        } finally {
            log.info("afterInvoke invokeState={}, url={}, context={}", invokeStage, url, JsonUtil.toJsonString(context));
        }
    }

    @Override
    public int order() {
        return 10000;
    }
}

package tech.obiteaaron.winter.embed.rpc.filter;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import tech.obiteaaron.winter.embed.registercenter.model.URL;
import tech.obiteaaron.winter.embed.rpc.constant.InvokerStage;
import tech.obiteaaron.winter.embed.rpc.executing.InvokeContext;

import java.util.List;

@Slf4j
public class LoggingWinterRpcFilter implements WinterRpcFilter {
    @Override
    public List<String> supportStageList() {
        return Lists.newArrayList(InvokerStage.CONSUMER.name(), InvokerStage.PROVIDER.name());
    }

    @Override
    public String beforeInvoke(URL url, InvokeContext context) {
        return null;
    }

    @Override
    public String afterInvoke(URL url, InvokeContext context) {
        return null;
    }
}

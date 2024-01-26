package tech.obiteaaron.winter.embed.rpc.filter.chain;

import tech.obiteaaron.winter.embed.registercenter.model.URL;
import tech.obiteaaron.winter.embed.rpc.executing.InvokeContext;

public interface FilterChain {

    public void invoke(String invokeStage, URL url, InvokeContext context);
}

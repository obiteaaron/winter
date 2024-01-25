package tech.obiteaaron.winter.embed.rpc.filter;

import org.jetbrains.annotations.NotNull;
import tech.obiteaaron.winter.embed.registercenter.model.URL;
import tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap;
import tech.obiteaaron.winter.embed.rpc.executing.InvokeContext;

import java.util.Comparator;
import java.util.List;

public interface RpcFilter extends Comparable<RpcFilter> {

    void setWinterRpcBootstrap(WinterRpcBootstrap winterRpcBootstrap);

    /**
     * 支持的阶段
     *
     * @see tech.obiteaaron.winter.embed.rpc.constant.InvokerStage
     */
    List<String> supportStageList();

    void beforeInvoke(String invokeStage, URL url, InvokeContext context);

    void afterInvoke(String invokeStage, URL url, InvokeContext context);

    /**
     * 数字越小，优先级越高，consumer和provider阶段都是
     *
     * @return
     */
    default int order() {
        return 0;
    }

    @Override
    default int compareTo(@NotNull RpcFilter o) {
        return Comparator.comparingInt(RpcFilter::order).compare(this, o);
    }
}

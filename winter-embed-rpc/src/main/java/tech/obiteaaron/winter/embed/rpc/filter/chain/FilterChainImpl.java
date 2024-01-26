package tech.obiteaaron.winter.embed.rpc.filter.chain;

import tech.obiteaaron.winter.embed.registercenter.model.URL;
import tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap;
import tech.obiteaaron.winter.embed.rpc.executing.InvokeContext;
import tech.obiteaaron.winter.embed.rpc.filter.RpcFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * 调用链
 */
public class FilterChainImpl implements FilterChain {

    private List<RpcFilter> rpcFilters;

    private RpcFilter realInvokeFilter;

    private int size;

    private int index = 0;

    @Override
    public void invoke(String invokeStage, URL url, InvokeContext context) {
        if (index < size) {
            rpcFilters.get(index++).invoke(invokeStage, url, context, this);
        } else {
            realInvokeFilter.invoke(invokeStage, url, context, this);
        }
    }

    public void setRpcFilters(List<RpcFilter> rpcFilters) {
        this.rpcFilters = new ArrayList<>(rpcFilters);
        // 优先级排序
        this.rpcFilters.sort(RpcFilter::compareTo);
        this.size = this.rpcFilters.size();
    }

    public void setRealInvokeFilter(RpcFilter realInvokeFilter) {
        this.realInvokeFilter = realInvokeFilter;
    }

    public static class RealInvokeFilter implements RpcFilter {

        private final Runnable runnable;

        public RealInvokeFilter(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void setWinterRpcBootstrap(WinterRpcBootstrap winterRpcBootstrap) {

        }

        @Override
        public List<String> supportStageList() {
            return null;
        }

        @Override
        public void invoke(String invokeStage, URL url, InvokeContext context, FilterChain filterChain) {
            runnable.run();
        }
    }
}

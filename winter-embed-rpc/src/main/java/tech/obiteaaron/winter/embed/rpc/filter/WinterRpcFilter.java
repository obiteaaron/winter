package tech.obiteaaron.winter.embed.rpc.filter;

import tech.obiteaaron.winter.embed.registercenter.model.URL;
import tech.obiteaaron.winter.embed.rpc.executing.InvokeContext;

import java.util.List;

public interface WinterRpcFilter {
    /**
     * 支持的阶段
     */
    List<String> supportStageList();

    String beforeInvoke(URL url, InvokeContext context);

    String afterInvoke(URL url, InvokeContext context);

    /**
     * 数字越小，优先级越高，consumer和provider阶段都是
     *
     * @return
     */
    default int order() {
        return 0;
    }

}

package tech.obiteaaron.winter.embed.rpc.router;

import org.jetbrains.annotations.NotNull;
import tech.obiteaaron.winter.embed.registercenter.model.URL;
import tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap;
import tech.obiteaaron.winter.embed.rpc.regesiter.ConsumerConfig;

import java.util.Comparator;
import java.util.List;

/**
 * 服务路由，客户端调用时，用来路由到服务端
 */
public interface ProviderRouter extends Comparable<ProviderRouter> {
    /**
     * @param consumerConfig 消费者信息
     * @param providerList   提供者信息
     * @return 返回的是列表，可以走多个路由规则，最终路由出结果即可
     */
    List<URL> resolve(ConsumerConfig consumerConfig, List<URL> providerList);

    void setWinterRpcBootstrap(WinterRpcBootstrap winterRpcBootstrap);

    /**
     * 数字越小，优先级越高，consumer和provider阶段都是
     *
     * @return
     */
    default int order() {
        return 0;
    }

    @Override
    default int compareTo(@NotNull ProviderRouter o) {
        return Comparator.comparingInt(ProviderRouter::order).compare(this, o);
    }
}

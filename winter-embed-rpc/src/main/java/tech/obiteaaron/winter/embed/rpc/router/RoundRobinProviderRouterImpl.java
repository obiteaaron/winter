package tech.obiteaaron.winter.embed.rpc.router;

import com.google.common.collect.Lists;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import tech.obiteaaron.winter.embed.registercenter.model.URL;
import tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap;
import tech.obiteaaron.winter.embed.rpc.regesiter.ConsumerConfig;

import java.util.List;

public class RoundRobinProviderRouterImpl implements ProviderRouter {

    @Setter
    WinterRpcBootstrap winterRpcBootstrap;

    int tick = 0;

    @Override
    public List<URL> resolve(ConsumerConfig consumerConfig, List<URL> providerList) {
        if (CollectionUtils.isEmpty(providerList)) {
            return providerList;
        }
        if (providerList.size() == 1) {
            return providerList;
        }
        int current = tick++ > 65536 ? tick = 0 : tick;
        return Lists.newArrayList(providerList.get(current % providerList.size()));
    }
}

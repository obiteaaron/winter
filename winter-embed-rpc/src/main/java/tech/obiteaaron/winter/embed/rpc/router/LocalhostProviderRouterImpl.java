package tech.obiteaaron.winter.embed.rpc.router;

import org.apache.commons.collections4.CollectionUtils;
import tech.obiteaaron.winter.embed.registercenter.model.URL;
import tech.obiteaaron.winter.embed.rpc.constant.IpAddressUtil;
import tech.obiteaaron.winter.embed.rpc.regesiter.ConsumerConfig;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class LocalhostProviderRouterImpl implements ProviderRouter {
    @Override
    public List<URL> resolve(ConsumerConfig consumerConfig, List<URL> providerList) {
        if (CollectionUtils.isEmpty(providerList)) {
            return providerList;
        }
        return providerList.stream().filter(item -> Objects.equals(item.getIp(), IpAddressUtil.getLocalIpv4ByNetCard())).collect(Collectors.toList());
    }
}

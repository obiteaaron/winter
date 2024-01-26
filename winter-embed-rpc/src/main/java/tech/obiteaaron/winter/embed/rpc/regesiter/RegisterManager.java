package tech.obiteaaron.winter.embed.rpc.regesiter;

import org.apache.commons.lang3.tuple.Pair;
import tech.obiteaaron.winter.embed.registercenter.model.URL;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public interface RegisterManager {

    void register(ProviderConfig providerConfig);

    void subscribe(ConsumerConfig consumerConfig);

    void unregister(ProviderConfig providerConfig);

    void unsubscribe(ConsumerConfig consumerConfig);

    List<URL> lookup(ConsumerConfig consumerConfig);

    void setRegisterService(tech.obiteaaron.winter.embed.registercenter.RegisterService registerService);

    void setWinterRpcBootstrap(tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap winterRpcBootstrap);

    /**
     * 获取服务对应的方法签名Map
     *
     * @param serviceName 服务名
     * @return methodSignature-->bean,Method
     */
    Map<String, Pair<Object, Method>> getServiceMethodSignatureMap(String serviceName);
}

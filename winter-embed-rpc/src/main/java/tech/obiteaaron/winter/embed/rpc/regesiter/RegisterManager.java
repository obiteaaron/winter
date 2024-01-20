package tech.obiteaaron.winter.embed.rpc.regesiter;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.ReflectionUtils;
import tech.obiteaaron.winter.embed.registercenter.RegisterService;
import tech.obiteaaron.winter.embed.registercenter.model.URL;
import tech.obiteaaron.winter.embed.rpc.constant.MethodUtil;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegisterManager {

    @Setter
    RegisterService registerService;

    @Getter
    Map<String, Map<String, Pair<Object, Method>>> providerMap = new ConcurrentHashMap<>();

    public void register(ProviderConfig providerConfig) {
        // TODO 注册到注册中心

        // 注册到本地
        ReflectionUtils.doWithMethods(providerConfig.getInterfaceClass(), method -> {
            String methodSignature = MethodUtil.generateMethodSignature(method);
            providerMap.compute(providerConfig.getInterfaceName(), (s, stringObjectMap) -> {
                if (stringObjectMap == null) {
                    Map<String, Pair<Object, Method>> map = new ConcurrentHashMap<>();
                    map.put(methodSignature, Pair.of(providerConfig.getInterfaceImpl(), method));
                    return map;
                }
                stringObjectMap.putIfAbsent(methodSignature, Pair.of(providerConfig.getInterfaceImpl(), method));
                return stringObjectMap;
            });
        });
    }

    public void subscribe(ConsumerConfig consumerConfig) {
        // 注册一下
        // 然后再订阅一下
    }

    public void unregister(ProviderConfig providerConfig) {

    }

    public void unsubscribe(ConsumerConfig consumerConfig) {

    }

    public List<URL> lookup(ConsumerConfig consumerConfig) {
        return null;
    }
}

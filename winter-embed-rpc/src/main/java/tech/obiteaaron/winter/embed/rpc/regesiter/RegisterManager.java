package tech.obiteaaron.winter.embed.rpc.regesiter;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.ReflectionUtils;
import tech.obiteaaron.winter.embed.registercenter.RegisterService;
import tech.obiteaaron.winter.embed.registercenter.model.URL;
import tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap;
import tech.obiteaaron.winter.embed.rpc.constant.MethodUtil;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RegisterManager {

    @Setter
    RegisterService registerService;

    @Setter
    WinterRpcBootstrap winterRpcBootstrap;

    @Getter
    Map<String, Map<String, Pair<Object, Method>>> providerMap = new ConcurrentHashMap<>();

    public void register(ProviderConfig providerConfig) {
        // 注册到注册中心
        Map<String, String> parameterMap = ImmutableMap.of(
                "version", providerConfig.getVersion(),
                "group", providerConfig.getGroup(),
                "type", "provider",
                "methodSignatures", generatorAllMethodSignature(providerConfig));
        if (winterRpcBootstrap.getLoadBalanceServer() != null) {
            parameterMap = new HashMap<>(parameterMap);
            parameterMap.put("loadBalanceServer", winterRpcBootstrap.getLoadBalanceServer());
        }
        URL url = URL.builder()
                .protocol(winterRpcBootstrap.getHttpProtocol())
                .ip(winterRpcBootstrap.getBindHost())
                .port(winterRpcBootstrap.getPort())
                .path(providerConfig.getInterfaceName())
                .parameterMap(parameterMap)
                .build();
        registerService.register(url);

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

    private String generatorAllMethodSignature(ProviderConfig providerConfig) {
        Class<?> interfaceClass = providerConfig.getInterfaceClass();
        for (Method declaredMethod : interfaceClass.getDeclaredMethods()) {
            MethodUtil.generateMethodSignature(declaredMethod);
        }
        return Arrays.stream(interfaceClass.getDeclaredMethods())
                .map(MethodUtil::generateMethodSignature)
                .collect(Collectors.joining(","));
    }

    public void subscribe(ConsumerConfig consumerConfig) {
        // 注册一下
        // 注册到注册中心
        URL url = URL.builder()
                .protocol(winterRpcBootstrap.getHttpProtocol())
                .ip(winterRpcBootstrap.getBindHost())
                .port(winterRpcBootstrap.getPort())
                .path(consumerConfig.getInterfaceName())
                .parameterMap(ImmutableMap.of(
                        "version", consumerConfig.getVersion(),
                        "group", consumerConfig.getGroup(),
                        "type", "consumer"))
                .build();
        registerService.register(url);
        // 然后再订阅一下
    }

    public void unregister(ProviderConfig providerConfig) {

    }

    public void unsubscribe(ConsumerConfig consumerConfig) {

    }

    public List<URL> lookup(ConsumerConfig consumerConfig) {
        URL url = URL.builder()
                .path(consumerConfig.getInterfaceName())
                .parameterMap(ImmutableMap.of(
                        "version", consumerConfig.getVersion(),
                        "group", consumerConfig.getGroup(),
                        "type", "provider"))
                .build();
        List<URL> urlList = registerService.lookup(url);
        // TODO 过滤方法，避免出现新上线的版本方法不兼容、不存在
        List<URL> providerUrlList = urlList.stream().filter(item -> "provider".equals(item.getParameterMap().get("type"))).collect(Collectors.toList());
        return providerUrlList;
    }
}

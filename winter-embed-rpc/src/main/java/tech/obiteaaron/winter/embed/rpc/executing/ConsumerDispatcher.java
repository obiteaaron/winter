package tech.obiteaaron.winter.embed.rpc.executing;

import com.google.common.collect.ImmutableMap;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import tech.obiteaaron.winter.common.tools.http.CommonOkHttpClient;
import tech.obiteaaron.winter.embed.registercenter.model.URL;
import tech.obiteaaron.winter.embed.rpc.WinterConsumer;
import tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap;
import tech.obiteaaron.winter.embed.rpc.constant.MethodUtil;
import tech.obiteaaron.winter.embed.rpc.regesiter.ConsumerConfig;
import tech.obiteaaron.winter.embed.rpc.router.ProviderRouter;
import tech.obiteaaron.winter.embed.rpc.serializer.WinterDeserializer;
import tech.obiteaaron.winter.embed.rpc.serializer.WinterSerializeFactory;
import tech.obiteaaron.winter.embed.rpc.serializer.WinterSerializer;

import java.lang.reflect.Method;
import java.util.List;

public class ConsumerDispatcher {

    @Setter
    CommonOkHttpClient commonOkHttpClient;

    @Setter
    WinterRpcBootstrap winterRpcBootstrap;

    public Object dispatch(Object proxy, Method method, Object[] args, WinterConsumer annotation) {
        String interfaceName = method.getDeclaringClass().getName();
        ConsumerConfig consumerConfig = ConsumerConfig.builder()
                .interfaceName(interfaceName)
                .group(annotation.group())
                .version(annotation.version())
                .build();
        // 查提供者
        List<URL> providerList = winterRpcBootstrap.getRegisterManager().lookup(consumerConfig);
        // 路由策略、有效性校验等等路由规则
        List<URL> providerListResolve = null;
        List<ProviderRouter> providerRouters = winterRpcBootstrap.getProviderRouters();
        if (CollectionUtils.isEmpty(providerRouters)) {
            providerListResolve = providerList;
        } else {
            for (ProviderRouter providerRouter : providerRouters) {
                providerListResolve = providerRouter.resolve(consumerConfig, providerList);
            }
        }
        if (CollectionUtils.isEmpty(providerListResolve)) {
            throw new RuntimeException("NoProvider " + interfaceName + ":" + annotation.version() + ":" + annotation.group());
        }
        URL providerUrl = providerListResolve.get(0);
        // 构造 InvokeContext
        InvokeContext invokeContext = new InvokeContext();
        invokeContext.setServiceName(consumerConfig.getInterfaceName());
        invokeContext.setMethodName(method.getName());
        invokeContext.setMethodSignature(MethodUtil.generateMethodSignature(method));
        invokeContext.setArguments(args);
        // 序列化参数
        String serializerSupports = providerUrl.getParameterMap().get("serializerSupports");
        String serializerType = WinterSerializeFactory.resolveSerializerType(serializerSupports, winterRpcBootstrap.getConsumerSerializerSupports(), winterRpcBootstrap.getDefaultSerializerType());
        invokeContext.setSerializerType(serializerType);
        WinterSerializer winterSerializer = WinterSerializeFactory.getWinterSerializer(serializerType);
        String serializerResult = winterSerializer.serializer(invokeContext);

        // 用服务提供者的IP或者负载均衡服务器的IP
        String ip = StringUtils.firstNonBlank(providerUrl.getParameterMap().get("loadBalanceServer"), providerUrl.getIp());
        // 如果有负载均衡服务器，可以直接将Provider的地址覆盖掉，这一段可以直接被覆盖掉，这是为了内网访问的，如果有负载均衡代理，可以直接替代为负载均衡的IP地址
        URL url = URL.builder()
                .protocol(providerUrl.getProtocol())
                .ip(ip)
                .port(providerUrl.getPort())
                .path(invokeContext.getServiceName())
                .parameterMap(ImmutableMap.of(
                        "methodSignature", invokeContext.getMethodSignature(),
                        "serializerType", serializerType
                ))
                .build();
        String invokeUrl = url.toString();
        // okhttp POST调用 vertx的端口
        String result = commonOkHttpClient.doPost(invokeUrl, serializerResult);
        WinterDeserializer winterDeserializer = WinterSerializeFactory.getWinterDeserializer(serializerType);
        // 这里再想想怎么处理更好
        Object deserializer = winterDeserializer.deserializer(result, false, new String[]{method.getGenericReturnType().getTypeName()}, null);
        if (deserializer instanceof Object[]) {
            return ((Object[]) deserializer)[0];
        }
        return deserializer;
    }
}

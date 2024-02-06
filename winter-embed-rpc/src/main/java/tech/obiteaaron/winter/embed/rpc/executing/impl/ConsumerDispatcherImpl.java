package tech.obiteaaron.winter.embed.rpc.executing.impl;

import com.google.common.collect.ImmutableMap;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import tech.obiteaaron.winter.common.tools.http.CommonOkHttpClient;
import tech.obiteaaron.winter.common.tools.trace.Slf4jMdcUtil;
import tech.obiteaaron.winter.embed.registercenter.model.URL;
import tech.obiteaaron.winter.embed.rpc.WinterConsumer;
import tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap;
import tech.obiteaaron.winter.embed.rpc.constant.InvokerStage;
import tech.obiteaaron.winter.embed.rpc.constant.MethodUtil;
import tech.obiteaaron.winter.embed.rpc.executing.ConsumerDispatcher;
import tech.obiteaaron.winter.embed.rpc.executing.InvokeContext;
import tech.obiteaaron.winter.embed.rpc.filter.chain.FilterChainImpl;
import tech.obiteaaron.winter.embed.rpc.regesiter.ConsumerConfig;
import tech.obiteaaron.winter.embed.rpc.router.ProviderRouter;
import tech.obiteaaron.winter.embed.rpc.serializer.WinterDeserializer;
import tech.obiteaaron.winter.embed.rpc.serializer.WinterSerializeFactory;
import tech.obiteaaron.winter.embed.rpc.serializer.WinterSerializer;

import java.lang.reflect.Method;
import java.util.List;

public class ConsumerDispatcherImpl implements ConsumerDispatcher {

    @Setter
    CommonOkHttpClient commonOkHttpClient;

    @Setter
    WinterRpcBootstrap winterRpcBootstrap;

    @Override
    public Object dispatch(Object proxy, Method method, Object[] args, WinterConsumer annotation) {
        String interfaceName = method.getDeclaringClass().getName();
        ConsumerConfig consumerConfig = ConsumerConfig.builder()
                .interfaceName(interfaceName)
                .group(annotation.group())
                .version(annotation.version())
                .tags(annotation.tags())
                .build();

        // 查提供者
        List<URL> providerList = winterRpcBootstrap.getRegisterManager().lookup(consumerConfig);
        // 路由策略、有效性校验等等路由规则
        URL providerUrl = resolveRouterUrl(annotation, providerList, consumerConfig, interfaceName);

        // 构造 InvokeContext
        InvokeContext invokeContext = new InvokeContext();
        invokeContext.setServiceName(consumerConfig.getInterfaceName());
        invokeContext.setMethodSignature(MethodUtil.generateMethodSignature(method));
        invokeContext.setArguments(args);
        invokeContext.setTraceId(Slf4jMdcUtil.getTraceId());
        invokeContext.setApplicationName(winterRpcBootstrap.getApplicationName());

        // 构造调用链
        FilterChainImpl filterChain = new FilterChainImpl();
        filterChain.setRpcFilters(winterRpcBootstrap.getRpcFilters());
        filterChain.setRealInvokeFilter(new FilterChainImpl.RealInvokeFilter(() -> {
            // 序列化参数
            String serializerSupports = providerUrl.getParameterMap().get("serializerSupports");
            String serializerType = WinterSerializeFactory.resolveSerializerType(serializerSupports, winterRpcBootstrap.getConsumerSerializerSupports(), winterRpcBootstrap.getSerializerType());
            WinterSerializer winterSerializer = WinterSerializeFactory.getWinterSerializer(serializerType);
            invokeContext.setSerializerType(serializerType);
            String serializedContext = winterSerializer.serializer(invokeContext);
            // 调用远程服务
            String result = doInvoke(invokeContext, providerUrl, serializedContext);
            invokeContext.setResult(result);
        }));

        filterChain.invoke(InvokerStage.CONSUMER.name(), providerUrl, invokeContext);

        // 反序列化，是否应该放在RealInvokeFilter里面？
        return deserializer(method, invokeContext.getSerializerType(), (String) invokeContext.getResult());
    }

    /**
     * 决策路由
     */
    @NotNull
    private URL resolveRouterUrl(WinterConsumer annotation, List<URL> providerList, ConsumerConfig consumerConfig, String interfaceName) {
        List<URL> providerListResolve = providerList;
        List<ProviderRouter> providerRouters = winterRpcBootstrap.getProviderRouters();
        if (!CollectionUtils.isEmpty(providerRouters)) {
            for (ProviderRouter providerRouter : providerRouters) {
                providerListResolve = providerRouter.resolve(consumerConfig, providerListResolve);
            }
        }
        if (CollectionUtils.isEmpty(providerListResolve)) {
            throw new RuntimeException("NoProvider " + interfaceName + ":" + annotation.version() + ":" + annotation.group());
        }
        return providerListResolve.get(0);
    }

    String doInvoke(InvokeContext invokeContext, URL providerUrl, String serializedContext) {
        // 用服务提供者的IP或者负载均衡服务器的IP
        String ip = StringUtils.firstNonBlank(providerUrl.getParameterMap().get("loadBalanceServer"), providerUrl.getIp());
        // 如果有负载均衡服务器，可以直接将Provider的地址覆盖掉，这一段可以直接被覆盖掉，这是为了内网访问的，如果有负载均衡代理，可以直接替代为负载均衡的IP地址
        URL url = URL.builder()
                .protocol(providerUrl.getProtocol())
                .ip(ip)
                .port(providerUrl.getPort())
                .path(providerUrl.getPath())
                .parameterMap(ImmutableMap.of(
                        "methodSignature", invokeContext.getMethodSignature(),
                        "serializerType", invokeContext.getSerializerType()
                ))
                .build();
        String invokeUrl = url.toString();
        // okhttp POST调用 vertx的端口
        // TODO 支持调用后短轮询获取结果，以突破网关、接口的timeout限制
        String result = commonOkHttpClient.doPost(invokeUrl, serializedContext);
        return result;
    }

    private Object deserializer(Method method, String serializerType, String result) {
        WinterDeserializer winterDeserializer = WinterSerializeFactory.getWinterDeserializer(serializerType);
        Object deserializer = winterDeserializer.deserializer(result, false, new String[]{method.getGenericReturnType().getTypeName()}, null);
        if (deserializer instanceof Object[]) {
            return ((Object[]) deserializer)[0];
        }
        return deserializer;
    }
}

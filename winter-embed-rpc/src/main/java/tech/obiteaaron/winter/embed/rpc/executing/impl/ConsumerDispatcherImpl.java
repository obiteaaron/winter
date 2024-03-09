package tech.obiteaaron.winter.embed.rpc.executing.impl;

import com.google.common.collect.ImmutableMap;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import tech.obiteaaron.winter.common.tools.http.CommonOkHttpClient;
import tech.obiteaaron.winter.common.tools.id.UuidGenerator;
import tech.obiteaaron.winter.common.tools.trace.Slf4jMdcUtils;
import tech.obiteaaron.winter.embed.registercenter.model.URL;
import tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap;
import tech.obiteaaron.winter.embed.rpc.async.AsyncActionEnum;
import tech.obiteaaron.winter.embed.rpc.constant.InvokerStage;
import tech.obiteaaron.winter.embed.rpc.constant.MethodUtil;
import tech.obiteaaron.winter.embed.rpc.executing.ConsumerDispatcher;
import tech.obiteaaron.winter.embed.rpc.executing.InnerInvokeContext;
import tech.obiteaaron.winter.embed.rpc.executing.InvokeContext;
import tech.obiteaaron.winter.embed.rpc.filter.chain.FilterChainImpl;
import tech.obiteaaron.winter.embed.rpc.regesiter.ConsumerConfig;
import tech.obiteaaron.winter.embed.rpc.router.ProviderRouter;
import tech.obiteaaron.winter.embed.rpc.serializer.WinterDeserializer;
import tech.obiteaaron.winter.embed.rpc.serializer.WinterSerializeFactory;
import tech.obiteaaron.winter.embed.rpc.serializer.WinterSerializer;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

public class ConsumerDispatcherImpl implements ConsumerDispatcher {

    @Setter
    CommonOkHttpClient commonOkHttpClient;

    @Setter
    WinterRpcBootstrap winterRpcBootstrap;

    @Override
    public Object dispatch(Object proxy, Method method, Object[] args, ConsumerConfig consumerConfig) {
        // 查提供者
        List<URL> providerList = winterRpcBootstrap.getRegisterManager().lookup(consumerConfig);
        // 路由策略、有效性校验等等路由规则
        URL providerUrl = resolveRouterUrl(consumerConfig, providerList);

        // 构造 InvokeContext
        InnerInvokeContext innerInvokeContext = new InnerInvokeContext();
        innerInvokeContext.setServiceName(consumerConfig.getInterfaceName());
        innerInvokeContext.setMethodSignature(MethodUtil.generateMethodSignature(method));
        innerInvokeContext.setArguments(args);
        innerInvokeContext.setTraceId(Slf4jMdcUtils.getTraceId());
        innerInvokeContext.setApplicationName(winterRpcBootstrap.getApplicationName());

        innerInvokeContext.setAsyncRequestId(generatorAsyncRequestId(consumerConfig));
        innerInvokeContext.setAsyncAction(AsyncActionEnum.EXECUTE.name());
        innerInvokeContext.setExecuteTimeout(consumerConfig.getExecuteTimeout());
        innerInvokeContext.setAsyncQueryInterval(consumerConfig.getAsyncQueryInterval());

        innerInvokeContext.setProviderUrl(providerUrl);
        innerInvokeContext.setConsumerConfig(consumerConfig);

        // 构造调用链
        FilterChainImpl filterChain = new FilterChainImpl();
        filterChain.setRpcFilters(winterRpcBootstrap.getRpcFilters());
        filterChain.setRealInvokeFilter(new FilterChainImpl.RealInvokeFilter(() -> {
            // 序列化参数
            String serializerSupports = providerUrl.getParameterMap().get("serializerSupports");
            String serializerType = WinterSerializeFactory.resolveSerializerType(serializerSupports, winterRpcBootstrap.getConsumerSerializerSupports(), winterRpcBootstrap.getSerializerType());
            WinterSerializer winterSerializer = WinterSerializeFactory.getWinterSerializer(serializerType);
            innerInvokeContext.setSerializerType(serializerType);
            InvokeContext executeInvokeContext = innerInvokeContext.toExecuteInvokeContext();
            String serializedContext = winterSerializer.serializer(executeInvokeContext);
            innerInvokeContext.setSerializedString(serializedContext);
            // 调用远程服务
            String result = doInvokeAsyncIfNecessary(innerInvokeContext);
            innerInvokeContext.setResult(result);
        }));

        filterChain.invoke(InvokerStage.CONSUMER.name(), providerUrl, innerInvokeContext);

        // 反序列化，是否应该放在RealInvokeFilter里面？
        return deserializer(method, innerInvokeContext.getSerializerType(), (String) innerInvokeContext.getResult());
    }

    private String generatorAsyncRequestId(ConsumerConfig consumerConfig) {
        if (!consumerConfig.isAsync()) {
            return null;
        }
        return UuidGenerator.generate();
    }

    /**
     * 决策路由
     */
    @NotNull
    private URL resolveRouterUrl(ConsumerConfig consumerConfig, List<URL> providerList) {
        List<URL> providerListResolve = providerList;
        List<ProviderRouter> providerRouters = winterRpcBootstrap.getProviderRouters();
        if (!CollectionUtils.isEmpty(providerRouters)) {
            for (ProviderRouter providerRouter : providerRouters) {
                providerListResolve = providerRouter.resolve(consumerConfig, providerListResolve);
            }
        }
        if (CollectionUtils.isEmpty(providerListResolve)) {
            throw new RuntimeException("NoProvider " + consumerConfig.getInterfaceName() + ":" + consumerConfig.getVersion() + ":" + consumerConfig.getGroup());
        }
        return providerListResolve.get(0);
    }

    String doInvokeAsyncIfNecessary(InnerInvokeContext innerInvokeContext) {
        // 用服务提供者的IP或者负载均衡服务器的IP
        URL providerUrl = innerInvokeContext.getProviderUrl();
        String ip = StringUtils.firstNonBlank(providerUrl.getParameterMap().get("loadBalanceServer"), providerUrl.getIp());
        // 如果有负载均衡服务器，可以直接将Provider的地址覆盖掉，这一段可以直接被覆盖掉，这是为了内网访问的，如果有负载均衡代理，可以直接替代为负载均衡的IP地址
        URL url = URL.builder()
                .protocol(providerUrl.getProtocol())
                .ip(ip)
                .port(providerUrl.getPort())
                .path(providerUrl.getPath())
                .parameterMap(ImmutableMap.of(
                        "methodSignature", innerInvokeContext.getMethodSignature(),
                        "serializerType", innerInvokeContext.getSerializerType()
                ))
                .build();
        String invokeUrl = url.toString();
        // okhttp POST调用 vertx的端口
        // 支持调用后短轮询获取结果，以突破网关、接口的timeout限制
        ConsumerConfig consumerConfig = innerInvokeContext.getConsumerConfig();
        String serializedContext = innerInvokeContext.getSerializedString();
        if (!consumerConfig.isAsync()) {
            return commonOkHttpClient.doPost(invokeUrl, serializedContext);
        } else {
            Function<String, String> function = (body) -> commonOkHttpClient.doPost(invokeUrl, body);
            return winterRpcBootstrap.getAsyncHelper().runAsyncForConsumer(innerInvokeContext, function);
        }
    }

    private Object deserializer(Method method, String serializerType, String result) {
        WinterDeserializer winterDeserializer = WinterSerializeFactory.getWinterDeserializer(serializerType);
        Object deserializer = winterDeserializer.deserializer(result);
        if (deserializer instanceof Object[]) {
            return ((Object[]) deserializer)[0];
        }
        return deserializer;
    }
}

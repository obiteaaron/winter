package tech.obiteaaron.winter.embed.rpc.executing.impl;

import io.vertx.core.http.HttpServerRequest;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import tech.obiteaaron.winter.embed.registercenter.model.URL;
import tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap;
import tech.obiteaaron.winter.embed.rpc.constant.InvokerStage;
import tech.obiteaaron.winter.embed.rpc.executing.InvokeContext;
import tech.obiteaaron.winter.embed.rpc.executing.ProviderDispatcher;
import tech.obiteaaron.winter.embed.rpc.filter.chain.FilterChainImpl;
import tech.obiteaaron.winter.embed.rpc.serializer.WinterDeserializer;
import tech.obiteaaron.winter.embed.rpc.serializer.WinterSerializeFactory;
import tech.obiteaaron.winter.embed.rpc.serializer.WinterSerializer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

public class ProviderDispatcherImpl implements ProviderDispatcher {

    @Setter
    WinterRpcBootstrap winterRpcBootstrap;

    @Setter
    ThreadPoolExecutor threadPoolExecutor;

    @Override
    public String dispatch(HttpServerRequest httpServerRequest, String body) {
        try {
            String sourceIp = httpServerRequest.getParam("sourceIp");
            URL url = URL.builder()
                    .protocol(httpServerRequest.scheme())
                    .ip(httpServerRequest.authority().host())
                    .port(httpServerRequest.authority().port())
                    .path(httpServerRequest.path().substring(1))
                    .parameterMap(URL.getParameterMap(httpServerRequest.query()))
                    .build();
            // 序列化方式
            String serializerType = StringUtils.firstNonBlank(httpServerRequest.getParam("serializerType"), winterRpcBootstrap.getSerializerType());
            InvokeContext invokeContext = deserialize(url, body, serializerType);

            // 构造调用链
            FilterChainImpl filterChain = new FilterChainImpl();
            filterChain.setRpcFilters(winterRpcBootstrap.getRpcFilters());
            filterChain.setRealInvokeFilter(new FilterChainImpl.RealInvokeFilter(() -> {
                // 调用远程服务
                Object result = doExecute(invokeContext);
                invokeContext.setResult(result);
            }));

            filterChain.invoke(InvokerStage.PROVIDER.name(), url, invokeContext);

            // 如果客户端需要查询结果，直接返回该字符串，不需要序列化
            if (invokeContext.getResult() instanceof String
                    && winterRpcBootstrap.getAsyncHelper().isConsumerNeedAsyncQueryResult((String) invokeContext.getResult())) {
                return (String) invokeContext.getResult();
            }
            // 序列化。序列化放在最后，而不是在RealInvokeFilter中，是为了方便用户扩展的Filter想要在拿到结果后进行增强修改而设计的。
            return serialize(invokeContext.getResult(), serializerType);
        } catch (Exception e) {
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
        }
    }

    private InvokeContext deserialize(URL url, String body, String serializerType) {
        // 反序列化
        String[] typeArray = null;
        if ("json".equals(serializerType)) {
            // JSON 特殊处理，二次序列化参数类型。影响性能，生产用hessian更好。
            String serviceName = Objects.requireNonNull(StringUtils.trimToNull(url.getPath()), "serviceName cannot be null");
            String methodSignature = Objects.requireNonNull(StringUtils.trimToNull(url.getParameterMap().get("methodSignature")), "methodSignature cannot be null");
            Map<String, Pair<Object, Method>> map = winterRpcBootstrap.getRegisterManager().getServiceMethodSignatureMap(serviceName);
            if (map == null) {
                throw new UnsupportedOperationException("NoProvider " + serviceName);
            }
            Pair<Object, Method> pair = map.get(methodSignature);
            if (pair == null) {
                throw new UnsupportedOperationException("NoProvider " + serviceName + "#" + methodSignature);
            }
            Method method = pair.getRight();
            typeArray = Arrays.stream(method.getGenericParameterTypes())
                    .map(item -> {
                        if (item instanceof Class) {
                            return ((Class) item).getCanonicalName();
                        }
                        return item.getTypeName();
                    }).toArray(String[]::new);
        }
        WinterDeserializer winterDeserializer = WinterSerializeFactory.getWinterDeserializer(serializerType);
        Objects.requireNonNull(StringUtils.trimToNull(body), "requestBody cannot be null");
        InvokeContext invokeContext = (InvokeContext) winterDeserializer.deserializer(body);

        return invokeContext;
    }

    private Object doExecute(InvokeContext invokeContext) {
        if (invokeContext.getAsyncRequestId() == null) {
            // 没有异步请求ID，则直接走同步逻辑
            return doExecute0(invokeContext);
        }
        // 走异步逻辑
        return winterRpcBootstrap.getAsyncHelper().runAsyncForProvider(invokeContext, threadPoolExecutor, () -> doExecute0(invokeContext));
    }

    private Object doExecute0(InvokeContext invokeContext) {
        try {
            String serviceName = Objects.requireNonNull(StringUtils.trimToNull(invokeContext.getServiceName()), "serviceName cannot be null");
            String methodSignature = Objects.requireNonNull(StringUtils.trimToNull(invokeContext.getMethodSignature()), "methodSignature cannot be null");
            Map<String, Pair<Object, Method>> map = winterRpcBootstrap.getRegisterManager().getServiceMethodSignatureMap(serviceName);
            Pair<Object, Method> pair = map.get(methodSignature);
            Object bean = pair.getLeft();
            Method method = pair.getRight();

            Object[] arguments = invokeContext.getArguments();

            return method.invoke(bean, arguments);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private String serialize(Object object, String serializerType) {
        // 反序列化基本的上下文结构
        WinterSerializer winterSerializer = WinterSerializeFactory.getWinterSerializer(serializerType);
        return winterSerializer.serializer(object);
    }
}

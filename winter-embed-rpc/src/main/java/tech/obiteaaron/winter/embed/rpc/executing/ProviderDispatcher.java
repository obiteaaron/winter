package tech.obiteaaron.winter.embed.rpc.executing;

import io.vertx.core.http.HttpServerRequest;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import tech.obiteaaron.winter.embed.registercenter.model.URL;
import tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap;
import tech.obiteaaron.winter.embed.rpc.constant.InvokerStage;
import tech.obiteaaron.winter.embed.rpc.filter.RpcFilter;
import tech.obiteaaron.winter.embed.rpc.serializer.WinterDeserializer;
import tech.obiteaaron.winter.embed.rpc.serializer.WinterSerializeFactory;
import tech.obiteaaron.winter.embed.rpc.serializer.WinterSerializer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ProviderDispatcher {

    @Setter
    WinterRpcBootstrap winterRpcBootstrap;

    public String dispatch(HttpServerRequest httpServerRequest, String body) {
        try {
            String sourceIp = httpServerRequest.getParam("sourceIp");
            URL url = URL.builder()
                    .protocol(httpServerRequest.scheme())
                    .ip(httpServerRequest.host())
                    .port(0)
                    .path(httpServerRequest.path().substring(1))
                    .parameterMap(URL.getParameterMap(httpServerRequest.query()))
                    .build();
            // 序列化方式
            String serializerType = StringUtils.firstNonBlank(httpServerRequest.getParam("serializerType"), winterRpcBootstrap.getDefaultSerializerType());
            InvokeContext invokeContext = deserialize(url, body, serializerType);
            // 执行前Filter
            doProviderBeforeFilter(url, invokeContext);
            // 执行调用本地实现方法
            Object result = doExecute(invokeContext);
            // 执行后Filter
            doProviderAfterFilter(url, invokeContext);
            // 序列化
            return serialize(result, serializerType);
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
            Map<String, Pair<Object, Method>> map = winterRpcBootstrap.getRegisterManager().getProviderMap().get(serviceName);
            if (map == null) {
                throw new UnsupportedOperationException("NoProvider " + serviceName);
            }
            Pair<Object, Method> pair = map.get(methodSignature);
            if (pair == null) {
                throw new UnsupportedOperationException("NoProvider " + serviceName + "#" + methodSignature);
            }
            Object bean = pair.getLeft();
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
        InvokeContext invokeContext = (InvokeContext) winterDeserializer.deserializer(body, false, new String[]{InvokeContext.class.getCanonicalName()}, typeArray);

        return invokeContext;
    }

    private void doProviderBeforeFilter(URL consumerUrl, InvokeContext invokeContext) {
        List<RpcFilter> rpcFilters = new ArrayList<>(winterRpcBootstrap.getRpcFilters());
        if (CollectionUtils.isNotEmpty(rpcFilters)) {
            rpcFilters.stream()
                    .filter(item -> item.supportStageList() != null && item.supportStageList().contains(InvokerStage.PROVIDER.name()))
                    .sorted()
                    .forEach(item -> item.beforeInvoke(InvokerStage.PROVIDER.name(), consumerUrl, invokeContext));
        }
    }

    private Object doExecute(InvokeContext invokeContext) {
        try {
            String serviceName = Objects.requireNonNull(StringUtils.trimToNull(invokeContext.getServiceName()), "serviceName cannot be null");
            String methodSignature = Objects.requireNonNull(StringUtils.trimToNull(invokeContext.getMethodSignature()), "methodSignature cannot be null");
            Map<String, Pair<Object, Method>> map = winterRpcBootstrap.getRegisterManager().getProviderMap().get(serviceName);
            Pair<Object, Method> pair = map.get(methodSignature);
            Object bean = pair.getLeft();
            Method method = pair.getRight();

            Object[] arguments = invokeContext.getArguments();

            Object result = method.invoke(bean, arguments);
            return result;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void doProviderAfterFilter(URL consumerUrl, InvokeContext invokeContext) {
        List<RpcFilter> rpcFilters = new ArrayList<>(winterRpcBootstrap.getRpcFilters());
        if (CollectionUtils.isNotEmpty(rpcFilters)) {
            rpcFilters.stream()
                    .filter(item -> item.supportStageList() != null && item.supportStageList().contains(InvokerStage.PROVIDER.name()))
                    .sorted(Comparator.comparingInt(RpcFilter::order).reversed())
                    .forEach(item -> item.afterInvoke(InvokerStage.PROVIDER.name(), consumerUrl, invokeContext));
        }
    }

    private String serialize(Object object, String serializerType) {
        // 反序列化基本的上下文结构
        WinterSerializer winterSerializer = WinterSerializeFactory.getWinterSerializer(serializerType);
        return winterSerializer.serializer(object);
    }
}

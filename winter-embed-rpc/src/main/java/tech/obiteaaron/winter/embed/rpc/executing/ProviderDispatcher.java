package tech.obiteaaron.winter.embed.rpc.executing;

import io.vertx.core.http.HttpServerRequest;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import tech.obiteaaron.winter.embed.registercenter.model.URL;
import tech.obiteaaron.winter.embed.rpc.regesiter.RegisterManager;
import tech.obiteaaron.winter.embed.rpc.serializer.WinterDeserializer;
import tech.obiteaaron.winter.embed.rpc.serializer.WinterSerializeFactory;
import tech.obiteaaron.winter.embed.rpc.serializer.WinterSerializer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

public class ProviderDispatcher {

    @Setter
    RegisterManager registerManager;

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
            String serializerType = httpServerRequest.getParam("serializerType");
            InvokeContext invokeContext = deserialize(url, body, serializerType);
            Object o = doExecute(invokeContext);
            // 序列化
            return serialize(o, serializerType);
        } catch (Exception e) {
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
        } finally {
            // TODO 监控
        }
    }

    private InvokeContext deserialize(URL url, String body, String serializerType) {
        // 反序列化
        String[] typeArray = null;
        if ("json".equals(serializerType)) {
            // JSON 特殊处理，二次序列化参数类型。影响性能，生产用hessian更好。
            String serviceName = url.getPath();
            String methodSignature = url.getParameterMap().get("methodSignature");
            Map<String, Pair<Object, Method>> map = registerManager.getProviderMap().get(serviceName);
            if (map == null) {
                throw new UnsupportedOperationException("bug no provider " + serviceName);
            }
            Pair<Object, Method> pair = map.get(methodSignature);
            if (pair == null) {
                throw new UnsupportedOperationException("bug no provider " + serviceName + "#" + methodSignature);
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
        InvokeContext invokeContext = (InvokeContext) winterDeserializer.deserializer(body, false, new String[]{InvokeContext.class.getCanonicalName()}, typeArray);

        return invokeContext;
    }

    private String serialize(Object object, String serializerType) {
        // 反序列化基本的上下文结构
        WinterSerializer winterSerializer = WinterSerializeFactory.getWinterSerializer(serializerType);
        return winterSerializer.serializer(object);
    }

    private Object doExecute(InvokeContext invokeContext) {
        try {
            String serviceName = invokeContext.getServiceName();
            String methodName = invokeContext.getMethodName();
            String methodSignature = invokeContext.getMethodSignature();
            Map<String, Pair<Object, Method>> map = registerManager.getProviderMap().get(serviceName);
            Pair<Object, Method> pair = map.get(methodSignature);
            Object bean = pair.getLeft();
            Method method = pair.getRight();

            Object[] arguments = invokeContext.getArguments();

            // TODO 加Filter
            Object result = method.invoke(bean, arguments);
            return result;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        } finally {
            // TODO 监控
        }
    }
}

package tech.obiteaaron.winter.embed.rpc.executing;

import com.fasterxml.jackson.databind.JavaType;
import io.vertx.core.http.HttpServerRequest;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import tech.obiteaaron.winter.common.tools.json.JsonUtil;
import tech.obiteaaron.winter.embed.rpc.regesiter.RegisterManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

public class ProviderDispatcher {

    @Setter
    RegisterManager registerManager;

    public String dispatch(HttpServerRequest httpServerRequest, String body) {
        try {
            String sourceIp = httpServerRequest.getParam("sourceIp");
            String uri = httpServerRequest.uri();
            // 消费者调用的的URL
            String consumerUrl = httpServerRequest.getParam("consumerUrl");
            // 序列化方式
            String serializer = httpServerRequest.getParam("serializer");
            InvokeContext invokeContext = deserialize(body, serializer);
            return doExecute(invokeContext);
        } catch (Exception e) {
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
        } finally {
            // TODO 监控
        }
    }

    private InvokeContext deserialize(String body, String serializer) {
        // 反序列化基本的上下文结构
        InvokeContext invokeContext = JsonUtil.parseObject(body, InvokeContext.class);
        // TODO 反序列化参数对象
        return invokeContext;
    }

    private String doExecute(InvokeContext invokeContext) {
        try {
            String serviceName = invokeContext.getServiceName();
            String methodName = invokeContext.getMethodName();
            String methodSignature = invokeContext.getMethodSignature();
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

            Object[] arguments = invokeContext.getArguments();
            // TODO 临时代码，要挪走
            Object[] objects = new Object[arguments.length];
            Type[] parameterTypes = method.getGenericParameterTypes();
            int i = 0;
            for (Object argument : arguments) {
                if (argument == null) {
                    objects[i++] = null;
                    continue;
                }
                String jsonString = JsonUtil.toJsonString(argument);
                JavaType javaType = JsonUtil.getTypeFactory().constructType(parameterTypes[i]);
                Object o = JsonUtil.parseObject(jsonString, javaType);
                objects[i++] = o;
            }

            // TODO 加Filter
            Object result = method.invoke(bean, objects);
            // TODO 序列化
            return JsonUtil.toJsonString(result);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        } finally {
            // TODO 监控
        }
    }
}

package tech.obiteaaron.winter.embed.rpc.executing;

import lombok.Setter;
import tech.obiteaaron.winter.common.tools.http.CommonOkHttpClient;
import tech.obiteaaron.winter.common.tools.http.OkHttpClientFactory;
import tech.obiteaaron.winter.embed.registercenter.model.URL;
import tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap;
import tech.obiteaaron.winter.embed.rpc.constant.MethodUtil;
import tech.obiteaaron.winter.embed.rpc.regesiter.ConsumerConfig;
import tech.obiteaaron.winter.embed.rpc.regesiter.RegisterManager;
import tech.obiteaaron.winter.embed.rpc.serializer.WinterDeserializer;
import tech.obiteaaron.winter.embed.rpc.serializer.WinterSerializeFactory;
import tech.obiteaaron.winter.embed.rpc.serializer.WinterSerializer;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

public class ConsumerDispatcher {

    CommonOkHttpClient commonOkHttpClient = OkHttpClientFactory.commonOkHttpClient();

    @Setter
    RegisterManager registerManager;

    @Setter
    ConsumerDispatcher consumerDispatcher;

    @Setter
    WinterRpcBootstrap winterRpcBootstrap;

    public Object dispatch(Object proxy, Method method, Object[] args) {
        ConsumerConfig consumerConfig = ConsumerConfig.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .build();
        // 查提供者
        List<URL> providerList = registerManager.lookup(consumerConfig);
        // TODO 加路由策略

        URL providerUrl = providerList.get(0);
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

        // 为空则走本地
        String ip = providerUrl.getIp() == null ? "127.0.0.1" : providerUrl.getIp();
        int port = providerUrl.getPort() <= 0 ? 7080 : providerUrl.getPort();

        // TODO http和https可选，默认http，如果认为http不安全，可以开启https开关
        // TODO http://%s:%s/ 这一段可以直接被覆盖掉，这是为了内网访问的，如果有负载均衡代理，可以直接替代为负载均衡的IP地址
        String url = String.format("http://%s:%s/%s?methodSignature=%s&serializerType=%s", ip, port, invokeContext.getServiceName(), invokeContext.getMethodSignature(), serializerType);
        // okhttp POST调用 vertx的端口
        String result = commonOkHttpClient.doPost(url, serializerResult);
        WinterDeserializer winterDeserializer = WinterSerializeFactory.getWinterDeserializer(serializerType);
        // 这里再想想怎么处理更好
        Object deserializer = winterDeserializer.deserializer(result, new Type[]{method.getGenericReturnType()});
        if (deserializer instanceof Object[]) {
            return ((Object[]) deserializer)[0];
        }
        return deserializer;
    }
}

package tech.obiteaaron.winter.embed.rpc.executing;

import lombok.Setter;
import tech.obiteaaron.winter.common.tools.http.CommonOkHttpClient;
import tech.obiteaaron.winter.common.tools.http.OkHttpClientFactory;
import tech.obiteaaron.winter.common.tools.json.JsonUtil;
import tech.obiteaaron.winter.embed.registercenter.model.URL;
import tech.obiteaaron.winter.embed.rpc.constant.MethodUtil;
import tech.obiteaaron.winter.embed.rpc.regesiter.ConsumerConfig;
import tech.obiteaaron.winter.embed.rpc.regesiter.RegisterManager;

import java.lang.reflect.Method;
import java.util.List;

public class ConsumerDispatcher {

    CommonOkHttpClient commonOkHttpClient = OkHttpClientFactory.commonOkHttpClient();

    @Setter
    RegisterManager registerManager;

    @Setter
    ConsumerDispatcher consumerDispatcher;

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
        // TODO 序列化参数

        // 为空则走本地
        String ip = providerUrl.getIp() == null ? "127.0.0.1" : providerUrl.getIp();
        int port = providerUrl.getPort() <= 0 ? 7080 : providerUrl.getPort();

        // TODO http和https可选，默认http，如果认为http不安全，可以开启https开关
        // TODO http://%s:%s/ 这一段可以直接被覆盖掉，这是为了内网访问的，如果有负载均衡代理，可以直接替代为负载均衡的IP地址
        String url = String.format("http://%s:%s/%s?methodSignature=%s", ip, port, invokeContext.getServiceName(), invokeContext.getMethodSignature());
        // okhttp POST调用 vertx的端口
        String result = commonOkHttpClient.doPost(url, JsonUtil.toJsonString(invokeContext));
        // TODO 反序列化结果
        return JsonUtil.parseObject(result, method.getReturnType());
    }
}

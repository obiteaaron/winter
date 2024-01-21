package tech.obiteaaron.winter.embed.rpc.executing;

import lombok.Setter;
import tech.obiteaaron.winter.common.tools.http.CommonOkHttpClient;
import tech.obiteaaron.winter.common.tools.http.OkHttpClientFactory;
import tech.obiteaaron.winter.common.tools.json.JsonUtil;
import tech.obiteaaron.winter.embed.registercenter.model.URL;
import tech.obiteaaron.winter.embed.rpc.regesiter.ConsumerConfig;
import tech.obiteaaron.winter.embed.rpc.regesiter.RegisterManager;

import java.lang.reflect.Method;
import java.util.List;

public class ConsumerDispatcher {

    @Setter
    RegisterManager registerManager;

    @Setter
    ConsumerDispatcher consumerDispatcher;

    public Object dispatch(Object proxy, Method method, Object[] args) {
        ConsumerConfig consumerConfig = ConsumerConfig.builder()
                .interfaceName(null)
                .build();
        String name = method.getName();
        // 查提供者
        List<URL> providerList = registerManager.lookup(consumerConfig);
        // TODO 加路由策略

//        URL url = providerList.get(0);
        // 构造 InvokeContext
        // 序列化参数
        // okhttp POST调用 vertx的端口
        // 反序列化结果
        CommonOkHttpClient commonOkHttpClient = OkHttpClientFactory.commonOkHttpClient();
        String url = "http://127.0.0.1:7080/tech.obiteaaron.winter.embed.rpc.TestService?methodSignature=findById(java.lang.String)";
        InvokeContext invokeContext = new InvokeContext();
        invokeContext.setServiceName("tech.obiteaaron.winter.embed.rpc.TestService");
        invokeContext.setMethodName("findById");
        invokeContext.setMethodSignature("findById(java.lang.String)");
        invokeContext.setArguments(new Object[]{"111"});
        String result = commonOkHttpClient.doPost(url, JsonUtil.toJsonString(invokeContext));
        return JsonUtil.parseObject(result, method.getReturnType());
    }
}

package tech.obiteaaron.winter.embed.rpc.executing;

import lombok.Setter;
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

        URL url = providerList.get(0);
        // 构造InvokeContext
        // 序列化参数
        // okhttp POST调用 vertx的端口
        // 反序列化结果
        return null;
    }
}

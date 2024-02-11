package tech.obiteaaron.winter.embed.rpc.executing;

import com.google.common.reflect.AbstractInvocationHandler;
import tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap;
import tech.obiteaaron.winter.embed.rpc.regesiter.ConsumerConfig;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;

/**
 * consumer代理bean的真实处理类
 */
public class ConsumerInvocationHandler extends AbstractInvocationHandler {

    ConsumerConfig consumerConfig;

    WinterRpcBootstrap winterRpcBootstrap;

    public ConsumerInvocationHandler(ConsumerConfig consumerConfig, WinterRpcBootstrap winterRpcBootstrap) {
        this.consumerConfig = consumerConfig;
        this.winterRpcBootstrap = winterRpcBootstrap;
    }

    @CheckForNull
    @Override
    protected Object handleInvocation(Object proxy, Method method, @Nullable Object[] args) throws Throwable {
        Object result = winterRpcBootstrap.getConsumerDispatcher().dispatch(proxy, method, args, consumerConfig);
        return result;
    }

}
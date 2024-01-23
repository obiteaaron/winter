package tech.obiteaaron.winter.embed.rpc.executing;

import com.google.common.reflect.AbstractInvocationHandler;
import tech.obiteaaron.winter.embed.rpc.WinterConsumer;
import tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;

/**
 * consumer代理bean的真实处理类
 */
public class ConsumerInvocationHandler extends AbstractInvocationHandler {

    WinterConsumer annotation;

    WinterRpcBootstrap winterRpcBootstrap;

    public ConsumerInvocationHandler(WinterConsumer annotation, WinterRpcBootstrap winterRpcBootstrap) {
        this.annotation = annotation;
        this.winterRpcBootstrap = winterRpcBootstrap;
    }

    @CheckForNull
    @Override
    protected Object handleInvocation(Object proxy, Method method, @Nullable Object[] args) throws Throwable {
        Object result = winterRpcBootstrap.getConsumerDispatcher().dispatch(proxy, method, args, annotation);
        return result;
    }

}
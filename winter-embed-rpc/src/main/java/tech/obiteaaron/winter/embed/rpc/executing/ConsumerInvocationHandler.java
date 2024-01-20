package tech.obiteaaron.winter.embed.rpc.executing;

import com.google.common.reflect.AbstractInvocationHandler;
import tech.obiteaaron.winter.embed.rpc.WinterConsumer;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;

/**
 * consumer代理bean的真实处理类
 */
public class ConsumerInvocationHandler extends AbstractInvocationHandler {

    WinterConsumer annotation;

    ConsumerDispatcher consumerDispatcher;

    public ConsumerInvocationHandler(WinterConsumer annotation) {
        this.annotation = annotation;
    }

    @CheckForNull
    @Override
    protected Object handleInvocation(Object proxy, Method method, @Nullable Object[] args) throws Throwable {
        Object result = consumerDispatcher.dispatch(proxy, method, args);
        return result;
    }

}
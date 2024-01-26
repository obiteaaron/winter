package tech.obiteaaron.winter.embed.rpc.executing;

import tech.obiteaaron.winter.embed.rpc.WinterConsumer;
import tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap;

import java.lang.reflect.Method;

public interface ConsumerDispatcher {

    Object dispatch(Object proxy, Method method, Object[] args, WinterConsumer annotation);

    void setWinterRpcBootstrap(WinterRpcBootstrap winterRpcBootstrap);
}

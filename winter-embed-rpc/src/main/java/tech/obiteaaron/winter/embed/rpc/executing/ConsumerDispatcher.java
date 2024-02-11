package tech.obiteaaron.winter.embed.rpc.executing;

import tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap;
import tech.obiteaaron.winter.embed.rpc.regesiter.ConsumerConfig;

import java.lang.reflect.Method;

public interface ConsumerDispatcher {

    Object dispatch(Object proxy, Method method, Object[] args, ConsumerConfig consumerConfig);

    void setWinterRpcBootstrap(WinterRpcBootstrap winterRpcBootstrap);
}

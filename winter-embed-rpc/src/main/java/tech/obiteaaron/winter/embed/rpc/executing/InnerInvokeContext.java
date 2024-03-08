package tech.obiteaaron.winter.embed.rpc.executing;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import tech.obiteaaron.winter.embed.registercenter.model.URL;
import tech.obiteaaron.winter.embed.rpc.async.AsyncActionEnum;
import tech.obiteaaron.winter.embed.rpc.regesiter.ConsumerConfig;
import tech.obiteaaron.winter.embed.rpc.serializer.WinterSerializer;

/**
 * 内部调用上下文，内部会使用这个对象做上下链路的参数、序列化工具、方法定义等透传，但不需要远程调用
 *
 * @author nomadic
 * @date 2024/03/08
 */
@Getter
@Setter
@Accessors(chain = true)
public class InnerInvokeContext extends InvokeContext {
    private URL providerUrl;
    private ConsumerConfig consumerConfig;
    private WinterSerializer winterSerializer;
    /**
     * 序列化的上下文结果，用于RPC调用
     */
    private String serializedContext;

    public InvokeContext toExecuteInvokeContext() {
        InvokeContext invokeContext = new InvokeContext();
        invokeContext.setApplicationName(this.getApplicationName());
        invokeContext.setTraceId(this.getTraceId());
        invokeContext.setServiceName(this.getServiceName());
        invokeContext.setMethodSignature(this.getMethodSignature());
        invokeContext.setSerializerType(this.getSerializerType());
        invokeContext.setArguments(this.getArguments());
        invokeContext.setResult(this.getResult());
        invokeContext.setAsyncRequestId(this.getAsyncRequestId());
        invokeContext.setAsyncAction(AsyncActionEnum.EXECUTE.name());
        invokeContext.setSyncTimeout(this.getSyncTimeout());
        invokeContext.setExtInfo(this.getExtInfo());

        return invokeContext;
    }

    public InvokeContext toQueryInvokeContext() {
        InvokeContext invokeContext = new InvokeContext();
        invokeContext.setApplicationName(this.getApplicationName());
        invokeContext.setTraceId(this.getTraceId());
        invokeContext.setServiceName(this.getServiceName());
        invokeContext.setMethodSignature(this.getMethodSignature());
        invokeContext.setSerializerType(this.getSerializerType());
        invokeContext.setResult(this.getResult());
        invokeContext.setAsyncRequestId(this.getAsyncRequestId());
        invokeContext.setAsyncAction(AsyncActionEnum.QUERY.name());
        invokeContext.setSyncTimeout(this.getSyncTimeout());
        invokeContext.setExtInfo(this.getExtInfo());

        return invokeContext;
    }
}
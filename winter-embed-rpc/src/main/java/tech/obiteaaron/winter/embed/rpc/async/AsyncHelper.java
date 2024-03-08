package tech.obiteaaron.winter.embed.rpc.async;

import tech.obiteaaron.winter.embed.rpc.executing.InnerInvokeContext;

import java.util.function.Function;

/**
 * 异步工具，主要是解决，如果你的RPC调用，因为不可控的原因，必须经过一个timeout比较低中间链路、安全网关时，如果你的接口超过这个时间就必然失败，
 * 但你的业务本身却可以接受这个结果，不得不通过编写提交+查询的方式来实现解决的场景。
 * 本工具可以帮你通过简单的方式实现类似的效果，只需要加一点点配置即可。
 *
 * @author nomadic
 * @since 2024/03/08
 */
public interface AsyncHelper {

    String runAsyncForConsumer(InnerInvokeContext innerInvokeContext, Function<String, String> function);
}

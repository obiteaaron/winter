package tech.obiteaaron.winter.embed.rpc.async;

import org.apache.commons.lang3.StringUtils;
import tech.obiteaaron.winter.embed.rpc.executing.InnerInvokeContext;
import tech.obiteaaron.winter.embed.rpc.executing.InvokeContext;
import tech.obiteaaron.winter.embed.rpc.regesiter.ConsumerConfig;

import java.util.function.Function;

/**
 * 异步工具，主要是解决，如果你的RPC调用，因为不可控的原因，必须经过一个timeout比较低中间链路、安全网关时，如果你的接口超过这个时间就必然失败，
 * 但你的业务本身却可以接受这个结果，不得不通过编写提交+查询的方式来实现解决的场景。
 * 本工具可以帮你通过简单的方式实现类似的效果，只需要加一点点配置即可。
 *
 * @author nomadic
 * @since 2024/03/08
 */
public class DefaultAsyncHelperImpl implements AsyncHelper {
    private final AsyncResultDistributeStorage asyncResultDistributeStorage;

    public DefaultAsyncHelperImpl(AsyncResultDistributeStorage asyncResultDistributeStorage) {
        this.asyncResultDistributeStorage = asyncResultDistributeStorage;
    }

    @Override
    public String runAsyncForConsumer(InnerInvokeContext innerInvokeContext, Function<String, String> function) {
        ConsumerConfig consumerConfig = innerInvokeContext.getConsumerConfig();
        int timeout = consumerConfig.getTimeout();
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeout * 1000L;
        // 第一次调用：执行
        String result = function.apply(innerInvokeContext.getSerializedContext());
        // 如果直接返回了结果则同步返回
        if (!isConsumerNeedAsyncQueryResult(result)) {
            return result;
        }
        // 否则进行轮询查询直到超时
        InvokeContext queryInvokeContext = innerInvokeContext.toQueryInvokeContext();
        String serializedContextQuery = innerInvokeContext.getWinterSerializer().serializer(queryInvokeContext);
        while (System.currentTimeMillis() <= endTime) {
            if (isConsumerNeedAsyncQueryResult(result)) {
                // 第x次调用：查询结果
                result = function.apply(serializedContextQuery);
            } else {
                // 如果有结果了，则直接返回
                return result;
            }
        }
        // 超时没返回结果
        throw new RuntimeException("consumer async executing timeout " + innerInvokeContext.getProviderUrl().toString());
    }

    private boolean isConsumerNeedAsyncQueryResult(String result) {
        return StringUtils.startsWith("AsyncRequestId:", result);
    }
}

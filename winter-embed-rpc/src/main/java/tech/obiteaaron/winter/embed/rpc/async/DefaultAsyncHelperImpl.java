package tech.obiteaaron.winter.embed.rpc.async;

import org.apache.commons.lang3.StringUtils;
import tech.obiteaaron.winter.common.tools.threadpool.NamedThreadFactory;
import tech.obiteaaron.winter.common.tools.threadpool.ThreadUtils;
import tech.obiteaaron.winter.embed.rpc.executing.InnerInvokeContext;
import tech.obiteaaron.winter.embed.rpc.executing.InvokeContext;
import tech.obiteaaron.winter.embed.rpc.regesiter.ConsumerConfig;
import tech.obiteaaron.winter.embed.rpc.serializer.WinterDeserializer;
import tech.obiteaaron.winter.embed.rpc.serializer.WinterSerializeFactory;
import tech.obiteaaron.winter.embed.rpc.serializer.WinterSerializer;

import java.util.HashMap;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 异步工具，主要是解决，如果你的RPC调用，因为不可控的原因，必须经过一个timeout比较低中间链路、安全网关时，如果你的接口超过这个时间就必然失败，
 * 但你的业务本身却可以接受这个结果，不得不通过编写提交+查询的方式来实现解决的场景。
 * 本工具可以帮你通过简单的方式实现类似的效果，只需要加一点点配置即可。
 *
 * @author nomadic
 * @since 2024/03/08
 */
public class DefaultAsyncHelperImpl implements AsyncHelper {
    private final ConcurrentHashMap<String, Future<Object>> WATCH_DOG_MAP = new ConcurrentHashMap<>();

    private final AsyncResultDistributeStorage asyncResultDistributeStorage;
    private final String ASYNC_REQUEST_ID_PREFIX = "AsyncRequestId:";

    public DefaultAsyncHelperImpl(AsyncResultDistributeStorage asyncResultDistributeStorage) {
        this.asyncResultDistributeStorage = asyncResultDistributeStorage;
        initWatchDog();
    }

    @Override
    public String runAsyncForConsumer(InnerInvokeContext innerInvokeContext, Function<String, String> function) {
        ConsumerConfig consumerConfig = innerInvokeContext.getConsumerConfig();
        int timeout = consumerConfig.getTimeout();
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeout * 1000L;
        // 第一次调用：执行
        String result = function.apply(innerInvokeContext.getSerializedString());
        // 如果直接返回了结果则同步返回
        if (!isConsumerNeedAsyncQueryResult(result)) {
            return result;
        }
        // 否则进行轮询查询直到超时
        InvokeContext queryInvokeContext = innerInvokeContext.toQueryInvokeContext();
        String serializedContextQuery = WinterSerializeFactory.getWinterSerializer(innerInvokeContext.getSerializerType()).serializer(queryInvokeContext);
        while (System.currentTimeMillis() <= endTime) {
            try {
                TimeUnit.MILLISECONDS.sleep(innerInvokeContext.getAsyncQueryInterval());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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

    @Override
    public boolean isConsumerNeedAsyncQueryResult(String result) {
        return StringUtils.startsWith(result, ASYNC_REQUEST_ID_PREFIX);
    }

    @Override
    public Object runAsyncForProvider(InvokeContext invokeContext, ThreadPoolExecutor threadPoolExecutor, Supplier<Object> realInvokeMethod) {
        if (AsyncActionEnum.EXECUTE.name().equals(invokeContext.getAsyncAction())) {
            // 此处需要工作线程池来执行，否则无法使用Future机制来实现异步。
            Future<Object> future = threadPoolExecutor.submit(realInvokeMethod::get);
            try {
                int executeTimeout = invokeContext.getExecuteTimeout();
                // 有结果的情况下直接返回
                return future.get(executeTimeout, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            } catch (TimeoutException ignore) {
                saveFuture(invokeContext.getAsyncRequestId(), future);
                // 超时则返回请求ID，说明还没有结果
                return ASYNC_REQUEST_ID_PREFIX + invokeContext.getAsyncRequestId();
            }
        } else if (AsyncActionEnum.QUERY.name().equals(invokeContext.getAsyncAction())) {
            Object result = findFutureResult(invokeContext.getAsyncRequestId());
            if (result == null) {
                // 没有结果则依然返回请求ID
                return ASYNC_REQUEST_ID_PREFIX + invokeContext.getAsyncRequestId();
            } else {
                // 有结果则返回结果
                return result;
            }
        } else {
            throw new UnsupportedOperationException(invokeContext.getAsyncAction());
        }
    }

    private void saveFuture(String asyncRequestId, Future<Object> future) {
        WATCH_DOG_MAP.put(asyncRequestId, future);
    }

    private Object findFutureResult(String asyncRequestId) {
        String result = asyncResultDistributeStorage.find(asyncRequestId);
        if (result == null) {
            return null;
        }
        WinterDeserializer winterDeserializer = WinterSerializeFactory.getWinterDeserializer("hessian");
        Object deserializer = winterDeserializer.deserializer(result, false, null, null);
        if (deserializer instanceof Throwable) {
            throw new RuntimeException((Throwable) deserializer);
        } else {
            return deserializer;
        }
    }

    private void initWatchDog() {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("RpcAsyncHelper"));
        ThreadUtils.registerForShutdown(scheduledExecutorService);
        scheduledExecutorService.scheduleAtFixedRate(this::doWatchDog, 1000, 50, TimeUnit.MILLISECONDS);
    }

    private void doWatchDog() {
        HashMap<String, Future<Object>> map = new HashMap<>(WATCH_DOG_MAP);
        map.forEach((k, v) -> {
            if (v.isDone()) {
                try {
                    Object result = v.get();
                    // 先序列化写进去
                    WinterSerializer winterSerializer = WinterSerializeFactory.getWinterSerializer("hessian");
                    String serializer = winterSerializer.serializer(result);
                    asyncResultDistributeStorage.save(k, serializer);
                    WATCH_DOG_MAP.remove(k);
                } catch (InterruptedException | ExecutionException e) {
                    // 作为结果写进去
                    WinterSerializer winterSerializer = WinterSerializeFactory.getWinterSerializer("hessian");
                    String serializer = winterSerializer.serializer(e);
                    asyncResultDistributeStorage.save(k, serializer);
                    WATCH_DOG_MAP.remove(k);
                }
            }
        });
    }
}

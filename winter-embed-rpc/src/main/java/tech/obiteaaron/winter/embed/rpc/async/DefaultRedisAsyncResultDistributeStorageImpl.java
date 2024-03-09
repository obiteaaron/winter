package tech.obiteaaron.winter.embed.rpc.async;

import tech.obiteaaron.winter.common.tools.lock.redis.RedisConnectionHolder;
import tech.obiteaaron.winter.embed.rpc.serializer.WinterDeserializer;
import tech.obiteaaron.winter.embed.rpc.serializer.WinterSerializeFactory;
import tech.obiteaaron.winter.embed.rpc.serializer.WinterSerializer;

/**
 * 异步结果分布式存储，方便多机环境下共享结果
 *
 * @author nomadic
 * @date 2024/03/08
 */
public class DefaultRedisAsyncResultDistributeStorageImpl implements AsyncResultDistributeStorage {

    private final String prefix = "AsyncResult:";

    @Override
    public void save(String id, Object value) {
        // 先序列化写进去
        WinterSerializer winterSerializer = WinterSerializeFactory.getWinterSerializer("hessian");
        String serializer = winterSerializer.serializer(value);
        RedisConnectionHolder.syncRedisCommands().set(prefix + id, serializer);
    }

    @Override
    public Object find(String id) {
        String value = RedisConnectionHolder.syncRedisCommands().get(prefix + id);
        WinterDeserializer winterDeserializer = WinterSerializeFactory.getWinterDeserializer("hessian");
        return winterDeserializer.deserializer(value, false, null, null);
    }
}

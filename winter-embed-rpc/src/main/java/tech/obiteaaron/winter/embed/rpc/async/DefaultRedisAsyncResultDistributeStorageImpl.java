package tech.obiteaaron.winter.embed.rpc.async;

import tech.obiteaaron.winter.common.tools.lock.redis.RedisConnectionHolder;

/**
 * 异步结果分布式存储，方便多机环境下共享结果
 *
 * @author nomadic
 * @date 2024/03/08
 */
public class DefaultRedisAsyncResultDistributeStorageImpl implements AsyncResultDistributeStorage {

    private final String prefix = "AsyncResult:";

    @Override
    public void save(String id, String value) {
        RedisConnectionHolder.syncRedisCommands().set(prefix + id, value);
    }

    @Override
    public String find(String id) {
        return RedisConnectionHolder.syncRedisCommands().get(prefix + id);
    }
}

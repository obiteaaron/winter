package tech.obiteaaron.winter.utils.distributed.lock.redis;

import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

public class RedisConnectionHolder {

    static RedisConnectionHolder redisConnectionHolder = null;

    StatefulRedisConnection<String, String> redisConnection;

    public RedisConnectionHolder(StatefulRedisConnection<String, String> redisConnection) {
        this.redisConnection = redisConnection;
        RedisConnectionHolder.redisConnectionHolder = this;
    }

    public StatefulRedisConnection<String, String> getRedisConnection() {
        return redisConnection;
    }

    public static RedisCommands<String, String> syncRedisCommands() {
        return RedisConnectionHolder.redisConnectionHolder.redisConnection.sync();
    }
}
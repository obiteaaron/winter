package tech.obiteaaron.winter.common.tools.lock.redis;

import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RedisConnectionHolder {

    static RedisConnectionHolder redisConnectionHolder = null;

    StatefulRedisConnection<String, String> redisConnection;

    public RedisConnectionHolder(StatefulRedisConnection<String, String> redisConnection) {
        this.redisConnection = redisConnection;
        RedisConnectionHolder.redisConnectionHolder = this;
    }

    public static RedisCommands<String, String> syncRedisCommands() {
        return RedisConnectionHolder.redisConnectionHolder.redisConnection.sync();
    }
}
package tech.obiteaaron.winter.utils.distributed.lock.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;

class RedisClientFactory {

    public static RedisClient createClient(String host,
                                           int port,
                                           String password,
                                           int database) {
        if (StringUtils.isBlank(password) || StringUtils.equalsAnyIgnoreCase(password, "null", "nvl", "nil")) {
            RedisURI redisURI = RedisURI.builder()
                    .withHost(host)
                    .withPort(port)
                    .withDatabase(database)
                    .withTimeout(Duration.ofSeconds(3))
                    .build();
            return RedisClient.create(redisURI);
        } else {
            RedisURI redisURI = RedisURI.builder()
                    .withHost(host)
                    .withPort(port)
                    .withDatabase(database)
                    .withPassword((CharSequence) password)
                    .withTimeout(Duration.ofSeconds(3))
                    .build();
            return RedisClient.create(redisURI);
        }
    }
}

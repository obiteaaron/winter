package tech.obiteaaron.winter.common.tools.lock.redis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RedisLockSpringAutoConfiguration.RedisLockProperty.class)
public class RedisLockSpringAutoConfiguration {
    /**
     * 这个Bean是Spring管理的，但Locks提供的是静态方法，所以如果启动顺序不正确，会导致无法正确获取到锁对象。
     * SpringMVC、常见的RPC中间件都是在Spring容器成功启动后才会对外暴露接口。
     * 建议使用方，对于业务使用中的服务提供、定时任务，都在{@link org.springframework.context.event.ContextRefreshedEvent} 事件之后才执行，以确保业务正确。
     * 如果一定要在Spring启动前就执行，可以通过注入该Bean到业务Bean中，以确保正确初始化了。
     *
     * @param redisLockProperty 配置属性
     * @return Redis客户端容器
     */
    @Bean
    @ConditionalOnProperty(value = "tech.obiteaaron.winter.distributed.lock.redis.host")
    public RedisConnectionHolder winterLockRedisClientHolder(RedisLockProperty redisLockProperty) {
        return new RedisConnectionHolder(
                RedisClientFactory.createClient(redisLockProperty.host,
                                redisLockProperty.port,
                                redisLockProperty.password,
                                redisLockProperty.database)
                        .connect()
        );
    }

    @Getter
    @Setter
    @ConfigurationProperties(prefix = "tech.obiteaaron.winter.distributed.lock.redis")
    public static class RedisLockProperty {
        private String host;
        private int port;
        private String password;
        private int database;
    }
}

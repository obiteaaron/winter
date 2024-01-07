package tech.obiteaaron.winter.common.tools.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 本地缓存，用于提升性能，减少数据库、Redis等集中存储中间件的RPC调用
 */
@SuppressWarnings("all")
public class LocalCacheFactory {

    private static final Cache<String, Cache<String, Optional<?>>> CACHE_FACTORY = Caffeine.newBuilder().maximumSize(2000).build();

    private static final int DEFAULT_EXPIRE_SECONDS = 300;

    /**
     * 默认本地缓存
     *
     * @param factoryKey 缓存工厂key，确保不冲突
     * @param cacheKey   缓存key，确保不冲突
     * @param function   缓存key转换为缓存value的回调方法
     * @param <T>        泛型
     * @return 缓存值，或者加载后的值
     */
    public static <T> T get(String factoryKey, String cacheKey, Function<String, T> function) {
        Cache<String, Optional<?>> cache = getCache(factoryKey);
        Optional<?> optional = cache.get(cacheKey, key -> Optional.ofNullable(function.apply(key)));
        return (T) optional.orElse(null);
    }

    /**
     * 默认本地缓存
     *
     * @param factoryKey    缓存工厂key，确保不冲突
     * @param cacheKey      缓存key，确保不冲突
     * @param function      缓存key转换为缓存value的回调方法
     * @param expireSeconds 过期时间，默认是300秒，如果需要自定义，可以传入此参数
     * @param <T>           泛型
     * @return 缓存值，或者加载后的值
     */
    public static <T> T get(String factoryKey, int expireSeconds, String cacheKey, Function<String, T> function) {
        Cache<String, Optional<?>> cache = getCache(factoryKey, expireSeconds);
        Optional<?> optional = cache.get(cacheKey, key -> Optional.ofNullable(function.apply(key)));
        return (T) optional.orElse(null);
    }

    /**
     * 过期某个缓存key的值
     *
     * @param factoryKey
     * @param cacheKey
     * @param <T>
     * @return
     */
    public static <T> T invalid(String factoryKey, String cacheKey) {
        Cache<String, Optional<?>> cache = getCache(factoryKey);
        Optional<?> oldValue = cache.getIfPresent(cacheKey);
        cache.invalidate(cacheKey);
        return (T) oldValue;
    }

    private static @NotNull <T> Cache<String, Optional<?>> getCache(String factoryKey) {
        return getCache(factoryKey, DEFAULT_EXPIRE_SECONDS);
    }

    /**
     * @param factoryKey
     * @param expireSeconds 默认300秒
     * @param <T>
     * @return
     */
    private static @NotNull <T> Cache<String, Optional<?>> getCache(String factoryKey, int expireSeconds) {
        if (expireSeconds <= 0) {
            expireSeconds = DEFAULT_EXPIRE_SECONDS;
        }
        int finalExpireSeconds = expireSeconds;
        return CACHE_FACTORY.get(factoryKey, k -> Caffeine.newBuilder().maximumSize(2000).expireAfterWrite(finalExpireSeconds, TimeUnit.SECONDS).build());
    }
}

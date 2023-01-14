package tech.obiteaaron.winter.utils.distributed.lock;

import tech.obiteaaron.winter.utils.distributed.lock.redis.RedisLockAndWatchDog;
import tech.obiteaaron.winter.utils.distributed.lock.redis.RedisLockSpringTransactionDelegate;

/**
 * 分布式锁工厂类
 */
public final class Locks {
    private Locks() {
    }

    public static Lock newRedisLock(String lockKey) {
        return new RedisLockAndWatchDog(lockKey);
    }

    public static Lock newRedisLockWithTransaction(String lockKey) {
        return new RedisLockSpringTransactionDelegate(new RedisLockAndWatchDog(lockKey));
    }
}

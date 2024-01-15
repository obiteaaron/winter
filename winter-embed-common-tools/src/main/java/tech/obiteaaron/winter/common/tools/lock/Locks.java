package tech.obiteaaron.winter.common.tools.lock;

import tech.obiteaaron.winter.common.tools.lock.redis.RedisLockAndWatchDog;
import tech.obiteaaron.winter.common.tools.lock.redis.RedisLockSpringTransactionDelegate;

/**
 * 分布式锁工厂类，使用锁完成后必须解锁
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

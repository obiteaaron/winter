package tech.obiteaaron.winter.common.tools.lock;

import tech.obiteaaron.winter.common.tools.lock.redis.RedisLockAndWatchDog;
import tech.obiteaaron.winter.common.tools.lock.redis.RedisLockSpringTransactionDelegate;

/**
 * 分布式锁工厂类，使用锁完成后必须解锁
 */
public final class Locks {
    private Locks() {
    }

    /**
     * 带WatchDog的Redis分布式锁，会自动续锁，必须在使用结束后释放锁，否则会导致无法解锁。宕机时会自动释放，不影响。
     *
     * @param lockKey 加锁的key
     * @return 锁实例
     */
    public static Lock newRedisLock(String lockKey) {
        return new RedisLockAndWatchDog(lockKey);
    }


    /**
     * 用Spring事务回调包装的Redis分布式锁，可以在事务完成阶段调用解锁，确保事务安全。
     *
     * @param lockKey 加锁的key
     * @return 锁实例
     */
    public static Lock newRedisLockWithTransaction(String lockKey) {
        return new RedisLockSpringTransactionDelegate(new RedisLockAndWatchDog(lockKey));
    }
}

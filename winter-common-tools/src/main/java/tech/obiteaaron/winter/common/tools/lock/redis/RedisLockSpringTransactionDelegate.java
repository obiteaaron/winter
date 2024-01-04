package tech.obiteaaron.winter.common.tools.lock.redis;

import org.springframework.core.Ordered;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tech.obiteaaron.winter.common.tools.lock.Lock;

import java.util.concurrent.TimeUnit;

/**
 * 对事务包装实现，方便在事务提交后才释放锁
 */
public class RedisLockSpringTransactionDelegate implements Lock {

    private Lock lock;

    public RedisLockSpringTransactionDelegate(Lock lock) {
        this.lock = lock;
    }

    @Override
    public boolean tryLock() {
        return lock.tryLock();
    }

    @Override
    public boolean tryLock(long millisecond) {
        return lock.tryLock(millisecond);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) {
        return lock.tryLock(time, unit);
    }

    @Override
    public void unlock() {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            // 如果有事务开着，注册一个事务完成回调
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    lock.unlock();
                }

                @Override
                public int getOrder() {
                    return Ordered.LOWEST_PRECEDENCE;
                }
            });
        } else {
            // 没有事务则直接解锁
            lock.unlock();
        }
    }

    @Override
    public boolean isLocked() {
        return lock.isLocked();
    }

    @Override
    public boolean isLockedSelf() {
        return lock.isLockedSelf();
    }
}

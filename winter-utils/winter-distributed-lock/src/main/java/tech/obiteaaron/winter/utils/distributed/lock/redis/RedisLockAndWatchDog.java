package tech.obiteaaron.winter.utils.distributed.lock.redis;

import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RedisLockAndWatchDog extends RedisLock {

    static final ConcurrentHashMap<String, RedisLockAndWatchDog> LOCK_WATCH_DOG_HOLDER = new ConcurrentHashMap<>();

    private static final String WATCH_DOG_LUA_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('EXPIRE', KEYS[1], 120) else return 0 end";

    public RedisLockAndWatchDog(String lockKey) {
        super(lockKey);
    }

    @Override
    public boolean tryLock() {
        return tryLock(0);
    }

    @Override
    public boolean tryLock(long millisecond) {
        return tryLock(millisecond, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) {
        boolean lock = super.tryLock(time, unit);
        if (lock) {
            LOCK_WATCH_DOG_HOLDER.put(lockKey, this);
        }
        return lock;
    }

    @Override
    public void unlock() {
        RedisLockAndWatchDog redisLockAndWatchDog = LOCK_WATCH_DOG_HOLDER.get(lockKey);
        if (redisLockAndWatchDog == null || redisLockAndWatchDog.currentThread != Thread.currentThread()) {
            // 不是当前线程，是无法解锁的，所以直接返回
            return;
        }
        LOCK_WATCH_DOG_HOLDER.remove(lockKey);
        super.unlock();
    }

    @Override
    public boolean isLocked() {
        return LOCK_WATCH_DOG_HOLDER.containsKey(lockKey) || super.isLocked();
    }

    @Override
    public boolean isLockedSelf() {
        RedisLockAndWatchDog redisLockAndWatchDog = LOCK_WATCH_DOG_HOLDER.get(lockKey);
        if (redisLockAndWatchDog != null && redisLockAndWatchDog.currentThread == currentThread) {
            // 当前线程锁的
            return true;
        }
        // 本机其他线程，或其他机器的线程
        return false;
    }

    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);

    static {
        EXECUTOR_SERVICE.scheduleWithFixedDelay(() -> {
            try {
                RedisCommands<String, String> redisCommands = RedisConnectionHolder.syncRedisCommands();
                for (Map.Entry<String, RedisLockAndWatchDog> dogEntry : LOCK_WATCH_DOG_HOLDER.entrySet()) {
                    String lockKey = dogEntry.getKey();
                    RedisLockAndWatchDog redisLockAndWatchDog = dogEntry.getValue();
                    Long eval = redisCommands.eval(WATCH_DOG_LUA_SCRIPT, ScriptOutputType.INTEGER, new String[]{lockKey}, redisLockAndWatchDog.getValue());
                    if (eval != null && eval == 1) {
                        log.info("Redis锁刷新成功 {}, {}", lockKey, redisLockAndWatchDog.currentThread);
                    } else {
                        log.warn("Redis锁刷新失败，锁不存在或者已经被人工清理，删除WatchDog信息 {}, {}", lockKey, redisLockAndWatchDog.currentThread);
                        LOCK_WATCH_DOG_HOLDER.remove(lockKey);
                    }
                }
            } catch (Throwable t) {
                log.error("", t);
            }
        }, 0, 10, TimeUnit.SECONDS);
    }
}

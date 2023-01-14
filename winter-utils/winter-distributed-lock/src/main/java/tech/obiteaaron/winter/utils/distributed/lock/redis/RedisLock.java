package tech.obiteaaron.winter.utils.distributed.lock.redis;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.sync.RedisCommands;
import tech.obiteaaron.winter.utils.distributed.lock.Lock;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

class RedisLock implements Lock {

    private static final String OK = "OK";

    private static final String UNLOCK_LUA_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    final String lockKey;

    final Thread currentThread;
    /**
     * 固定120秒，WatchDog自动刷新也是120秒
     */
    final int lockedSecond = 120;

    public RedisLock(String lockKey) {
        Objects.requireNonNull(lockKey);
        this.lockKey = lockKey;
        currentThread = Thread.currentThread();
    }

    @Override
    public boolean tryLock() {
        return tryLock0(0);
    }

    @Override
    public boolean tryLock(long millisecond) {
        return tryLock0(millisecond);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) {
        return tryLock0(unit.toMillis(time));
    }

    private boolean tryLock0(long millisecond) {
        if (millisecond < 0) {
            millisecond = 0;
        }
        long startTime = System.currentTimeMillis();
        long endTime = startTime + millisecond;
        long sleepTime = millisecond / 3;
        RedisCommands<String, String> redisCommands = RedisConnectionHolder.syncRedisCommands();
        while (true) {
            String setResult = redisCommands.set(lockKey, getValue(), SetArgs.Builder.nx().ex(lockedSecond));
            // 加锁成功
            if (OK.equals(setResult)) {
                return true;
            }
            // 时间到了则直接返回
            long retainMillisecond = endTime - System.currentTimeMillis();
            if (retainMillisecond <= 0) {
                return false;
            }
            // 等一会再重试
            long realSleepTime = Math.min(sleepTime, retainMillisecond);
            try {
                TimeUnit.MILLISECONDS.sleep(realSleepTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void unlock() {
        RedisCommands<String, String> redisCommands = RedisConnectionHolder.syncRedisCommands();
        redisCommands.eval(UNLOCK_LUA_SCRIPT, ScriptOutputType.INTEGER, new String[]{lockKey}, getValue());
    }

    String getValue() {
        return currentThread.hashCode() + ":" + getIpAddr();
    }

    private static final Cache<String, String> LOCAL_IP_CACHE = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();

    private String getIpAddr() {
        try {
            return LOCAL_IP_CACHE.get("IP", RedisLock::getLocalIpByNetCard);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getLocalIpByNetCard() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                List<InterfaceAddress> interfaceAddresses = networkInterface.getInterfaceAddresses();
                for (InterfaceAddress interfaceAddress : interfaceAddresses) {
                    if (interfaceAddress.getAddress() instanceof Inet4Address) {
                        return interfaceAddress.getAddress().getHostAddress();
                    }
                }
            }
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    @Override
    public boolean isLocked() {
        return RedisConnectionHolder.syncRedisCommands().exists(lockKey) > 0;
    }

    @Override
    public boolean isLockedSelf() {
        String value = RedisConnectionHolder.syncRedisCommands().get(lockKey);
        return Objects.equals(value, getValue());
    }
}

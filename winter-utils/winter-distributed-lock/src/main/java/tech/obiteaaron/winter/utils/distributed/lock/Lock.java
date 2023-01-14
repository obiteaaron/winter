package tech.obiteaaron.winter.utils.distributed.lock;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁接口
 */
public interface Lock extends Closeable {
    /**
     * 尝试加锁一次，不阻塞等待
     *
     * @return true 加锁成功 | false 加锁失败
     */
    boolean tryLock();

    /**
     * 尝试加锁，阻塞等待参数提供的毫秒数
     *
     * @param millisecond 最大等待毫秒数
     * @return true 加锁成功 | false 加锁失败
     */
    boolean tryLock(long millisecond);

    /**
     * 尝试加锁，阻塞等待参数提供的毫秒数
     *
     * @param time 等待时长
     * @param unit 等待时间单位
     * @return true 加锁成功 | false 加锁失败
     */
    boolean tryLock(long time, TimeUnit unit);

    /**
     * 解锁，如果当前线程持有锁则解锁，否则不做任何处理。
     * 注意：锁必须被正确释放，未释放的锁都会被WatchDog不断刷新，直到解锁或当前容器服务下线。
     */
    void unlock();

    /**
     * 是否被锁住，用于检测当前key是否被自己或其他线程锁住
     *
     * @return true 被锁住 | false 没有被锁住
     */
    boolean isLocked();

    /**
     * 是否被自身锁住，用于检测当前线程是否持有锁
     *
     * @return true 被锁住 | false 没有被锁住
     */
    boolean isLockedSelf();

    /**
     * 用于支持在try-with-resources中使用
     */
    @Override
    default void close() {
        unlock();
    }
}

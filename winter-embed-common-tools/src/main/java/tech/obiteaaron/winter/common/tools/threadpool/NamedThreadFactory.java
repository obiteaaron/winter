package tech.obiteaaron.winter.common.tools.threadpool;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {

    private final String name;

    private final AtomicInteger index = new AtomicInteger();

    public NamedThreadFactory(String name) {
        this.name = name;
    }

    @Override
    public Thread newThread(@NotNull Runnable r) {
        // 序号从1开始
        return new Thread(r, "ThreadPool_" + name + "_" + index.incrementAndGet());
    }
}

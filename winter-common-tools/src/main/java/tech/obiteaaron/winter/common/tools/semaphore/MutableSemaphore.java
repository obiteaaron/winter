package tech.obiteaaron.winter.common.tools.semaphore;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

/**
 * 可变信号量
 */
@Slf4j
public class MutableSemaphore {

    private static final int MIN_PERMITS = 1;
    private static final int MAX_PERMITS = 1000;

    private final InnerSemaphore semaphore = new InnerSemaphore(0);

    private final String name;

    private int permits;

    private final Supplier<Integer> permitsSupplier;

    MutableSemaphore(String name, Supplier<Integer> permitsSupplier) {
        this.name = Objects.requireNonNull(name);
        this.permitsSupplier = Objects.requireNonNull(permitsSupplier);
        Objects.requireNonNull(permitsSupplier.get());
        this.permits = calcNewPermits();
        // 放信号量进去
        this.semaphore.release(permits);
    }

    public Semaphore get() {
        return semaphore;
    }

    private int calcNewPermits() {
        return Math.max(MIN_PERMITS, Math.min(Optional.ofNullable(permitsSupplier.get()).orElse(1), MAX_PERMITS));
    }

    void refresh() {
        int newPermits = calcNewPermits();
        if (newPermits == permits) {
            return;
        }
        log.info("MutableSemaphore Change name = {}, value = {} --> {}", name, permits, newPermits);
        if (newPermits < permits) {
            // 缩容
            this.semaphore.reducePermits(permits - newPermits);
        } else {
            // 扩容
            this.semaphore.release(newPermits - permits);
        }
    }

    private static class InnerSemaphore extends Semaphore {

        private static final long serialVersionUID = 5064355439178288965L;

        public InnerSemaphore(int permits) {
            super(permits);
        }

        public InnerSemaphore(int permits, boolean fair) {
            super(permits, fair);
        }

        @Override
        protected void reducePermits(int reduction) {
            super.reducePermits(reduction);
        }
    }
}

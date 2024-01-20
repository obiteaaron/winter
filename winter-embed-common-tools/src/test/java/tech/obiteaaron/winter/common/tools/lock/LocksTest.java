package tech.obiteaaron.winter.common.tools.lock;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class LocksTest {

    @Test
    public void testSingleThreadLockAndUnlock() {
        for (int i = 0; i < 1000; i++) {
            try (Lock lock = Locks.newRedisLock("lock:test1")) {
                boolean tryLock = lock.tryLock();
                Assert.assertTrue(tryLock);
            }
            // 上面已经释放了，再次锁
            try (Lock lock = Locks.newRedisLock("lock:test1")) {
                boolean tryLock = lock.tryLock();
                Assert.assertTrue(tryLock);
            }
            // 上面已经释放了，再次锁
            try (Lock lock = Locks.newRedisLock("lock:test1")) {
                boolean tryLock = lock.tryLock();
                Assert.assertTrue(tryLock);

                // 不允许重入，如果有类似场景，建议采用两个不同的锁实现
                try (Lock lock1 = Locks.newRedisLock("lock:test1")) {
                    boolean tryLock1 = lock1.tryLock();
                    Assert.assertFalse(tryLock1);
                }
            }
        }
        for (int i = 0; i < 100; i++) {
            // 上面已经释放了，再次锁
            try (Lock lock = Locks.newRedisLock("lock:test1")) {
                boolean tryLock = lock.tryLock();
                Assert.assertTrue(tryLock);

                // 不允许重入，如果有类似场景，建议采用两个不同的锁实现
                try (Lock lock1 = Locks.newRedisLock("lock:test1")) {
                    boolean tryLock1 = lock1.tryLock(10);
                    Assert.assertFalse(tryLock1);
                }
            }
        }
    }

    @Test
    public void testMultiThreadLockAndUnlock() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 2000; i++) {
            executorService.submit(() -> {
                try (Lock lock = Locks.newRedisLock("lock:test1")) {
                    boolean tryLock = lock.tryLock();
//                    System.out.println(tryLock);
                    if (tryLock) {
                        // 不允许重入，如果有类似场景，建议采用两个不同的锁实现
                        try (Lock lock1 = Locks.newRedisLock("lock:test1")) {
                            boolean tryLock1 = lock1.tryLock();
                            Assert.assertFalse(tryLock1);
                        }
                    }
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
    }

    @Test
    public void testParentChildThreadLockAndUnlock() throws InterruptedException {
        ExecutorService executorServiceParent = Executors.newFixedThreadPool(10);
        ExecutorService executorServiceChild = Executors.newFixedThreadPool(10);
        for (int i1 = 0; i1 < 2000; i1++) {
            executorServiceParent.submit(() -> {
                for (int i = 0; i < 2000; i++) {
                    executorServiceChild.submit(() -> {
                        try (Lock lock = Locks.newRedisLock("lock:test1")) {
                            boolean tryLock = lock.tryLock();
//                            System.out.println(tryLock);
                            if (tryLock) {
                                // 不允许重入，如果有类似场景，建议采用两个不同的锁实现
                                try (Lock lock1 = Locks.newRedisLock("lock:test1")) {
                                    boolean tryLock1 = lock1.tryLock();
                                    Assert.assertFalse(tryLock1);
                                }
                            }
                        }
                    });
                }
            });
        }
        executorServiceChild.shutdown();
        executorServiceChild.awaitTermination(1, TimeUnit.MINUTES);
        executorServiceParent.shutdown();
        executorServiceParent.awaitTermination(1, TimeUnit.MINUTES);
    }

    @Test
    public void testWatchDog() throws InterruptedException {
        try (Lock lock1 = Locks.newRedisLockWithTransaction("lock:test1")) {
            boolean tryLock1 = lock1.tryLock(10);
            Assert.assertTrue(tryLock1);
            TimeUnit.SECONDS.sleep(30);
            Assert.assertTrue(lock1.isLocked());
            Assert.assertTrue(lock1.isLockedSelf());
        }
    }

    @Test
    public void testSpringLockAndUnlock() {
        for (int i = 0; i < 1000; i++) {
            try (Lock lock1 = Locks.newRedisLockWithTransaction("lock:test1")) {
                boolean tryLock1 = lock1.tryLock(10);
                Assert.assertTrue(tryLock1);
            }
        }
    }
//
//    @Test
//    @Transactional
//    public void testSpringLockAndUnlock2() {
//        for (int i = 0; i < 1000; i++) {
//            try (Lock lock1 = Locks.newRedisLockWithTransaction("lock:test1")) {
//                boolean tryLock1 = lock1.tryLock(10);
//                Assert.assertTrue(tryLock1);
//            }
//        }
//    }
}

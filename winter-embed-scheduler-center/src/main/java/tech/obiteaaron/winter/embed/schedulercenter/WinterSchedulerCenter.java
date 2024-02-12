package tech.obiteaaron.winter.embed.schedulercenter;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap;
import tech.obiteaaron.winter.embed.schedulercenter.executor.BeanParser;
import tech.obiteaaron.winter.embed.schedulercenter.executor.WinterSchedulerExecutor;
import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJob;
import tech.obiteaaron.winter.embed.schedulercenter.repository.WinterJobInstanceRepository;
import tech.obiteaaron.winter.embed.schedulercenter.repository.WinterJobInstanceTaskRepository;
import tech.obiteaaron.winter.embed.schedulercenter.repository.WinterJobRepository;
import tech.obiteaaron.winter.embed.schedulercenter.repository.impl.memory.WinterJobInstanceMemoryRepositoryImpl;
import tech.obiteaaron.winter.embed.schedulercenter.repository.impl.memory.WinterJobMemoryRepositoryImpl;
import tech.obiteaaron.winter.embed.schedulercenter.scheduler.WinterSchedulerDispatcher;
import tech.obiteaaron.winter.embed.schedulercenter.scheduler.WinterSchedulerRegister;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public final class WinterSchedulerCenter {
    /**
     * 单例，无需多例
     */
    public static final WinterSchedulerCenter INSTANCE = new WinterSchedulerCenter();

    private final AtomicBoolean initialized = new AtomicBoolean();

    @Getter
    private WinterSchedulerCenterConfig winterSchedulerCenterConfig = new WinterSchedulerCenterConfig();

    @Getter
    private WinterJobRepository winterJobRepository = new WinterJobMemoryRepositoryImpl();

    @Getter
    private WinterJobInstanceRepository winterJobInstanceRepository = new WinterJobInstanceMemoryRepositoryImpl();

    private WinterJobInstanceTaskRepository winterJobInstanceTaskRepository;
    /**
     * 基于RPC的实例提供集群内的多机分发功能，以实现Map、MapReduce任务功能
     */
    @Getter
    private WinterRpcBootstrap winterRpcBootstrap;

    @Getter
    private BeanParser beanParser;

    @Getter
    private final WinterSchedulerRegister winterSchedulerRegister = new WinterSchedulerRegister();

    @Getter
    private final WinterSchedulerDispatcher winterSchedulerDispatcher = new WinterSchedulerDispatcher();

    @Getter
    private final WinterSchedulerExecutor winterSchedulerExecutor = new WinterSchedulerExecutor();

    private WinterSchedulerCenter() {
    }

    public WinterJob addWinterJob(JobProcessor jobProcessor) {
        return winterSchedulerRegister.addWinterJob(jobProcessor);
    }

    public WinterJob addWinterJob(WinterJob winterJob) {
        return winterSchedulerRegister.addWinterJob(winterJob);
    }

    public void triggerManual(WinterJob winterJob, String manualParams) {
        winterSchedulerDispatcher.triggerManual(winterJob, manualParams);
    }

    public void start() {
        if (!initialized.compareAndSet(false, true)) {
            return;
        }
        int delayStartMillisecond = winterSchedulerCenterConfig.getDelayStartMillisecond();
        if (delayStartMillisecond > 0) {
            // 延迟启动
            new Thread(() -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(delayStartMillisecond);
                } catch (InterruptedException ignore) {
                }
                realStart();
            }).start();
        } else {
            // 不延迟启动
            realStart();
        }
    }

    private void realStart() {
        // 启动注册服务
        winterSchedulerRegister.setWinterJobRepository(Objects.requireNonNull(winterJobRepository, "winterJobRepository cannot be null"));
        winterSchedulerRegister.setWinterSchedulerCenter(this);
        winterSchedulerRegister.start();
        // 等待注册服务启动后，将Map类的服务提供者注册上去。
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // 启动调度执行服务
        winterSchedulerDispatcher.setWinterJobRepository(Objects.requireNonNull(winterJobRepository, "winterJobRepository cannot be null"));
        winterSchedulerDispatcher.setWinterJobInstanceRepository(Objects.requireNonNull(winterJobInstanceRepository, "winterJobInstanceRepository cannot be null"));
        winterSchedulerDispatcher.setWinterJobInstanceTaskRepository(winterJobInstanceTaskRepository);
        winterSchedulerDispatcher.setBeanParser(Objects.requireNonNull(beanParser, "beanParser cannot be null"));
        winterSchedulerDispatcher.setWinterSchedulerCenter(this);
        winterSchedulerDispatcher.start();
        // 配置工作线程池大小
        winterSchedulerExecutor.setPoolSize(winterSchedulerCenterConfig.getThreadPoolSize());
        winterSchedulerExecutor.setWinterJobInstanceRepository(winterJobInstanceRepository);
        // TODO 初始化RPC独享实例，用Map、MapReduce任务
        if (winterSchedulerCenterConfig.isEnableMapJobClusterRpc() && winterRpcBootstrap == null) {
//        winterRpcBootstrap.start();
        }
    }

    public WinterSchedulerCenter setWinterSchedulerCenterConfig(WinterSchedulerCenterConfig winterSchedulerCenterConfig) {
        this.winterSchedulerCenterConfig = winterSchedulerCenterConfig;
        return this;
    }

    public WinterSchedulerCenter setWinterJobRepository(WinterJobRepository winterJobRepository) {
        this.winterJobRepository = Objects.requireNonNull(winterJobRepository, "winterJobRepository cannot be null");
        return this;
    }

    public WinterSchedulerCenter setWinterJobInstanceRepository(WinterJobInstanceRepository winterJobInstanceRepository) {
        this.winterJobInstanceRepository = Objects.requireNonNull(winterJobInstanceRepository, "winterJobInstanceRepository cannot be null");
        return this;
    }

    public WinterSchedulerCenter setWinterJobInstanceTaskRepository(WinterJobInstanceTaskRepository winterJobInstanceTaskRepository) {
        this.winterJobInstanceTaskRepository = Objects.requireNonNull(winterJobInstanceTaskRepository, "winterJobInstanceTaskRepository cannot be null");
        return this;
    }

    public WinterSchedulerCenter setWinterRpcBootstrap(WinterRpcBootstrap winterRpcBootstrap) {
        this.winterRpcBootstrap = winterRpcBootstrap;
        return this;
    }

    public WinterSchedulerCenter setBeanParser(BeanParser beanParser) {
        this.beanParser = Objects.requireNonNull(beanParser, "beanParser cannot be null");
        return this;
    }
}

package tech.obiteaaron.winter.embed.schedulercenter;

import lombok.extern.slf4j.Slf4j;
import tech.obiteaaron.winter.embed.rpc.WinterRpcBootstrap;
import tech.obiteaaron.winter.embed.schedulercenter.executor.BeanParser;
import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJob;
import tech.obiteaaron.winter.embed.schedulercenter.repository.WinterJobInstanceRepository;
import tech.obiteaaron.winter.embed.schedulercenter.repository.WinterJobInstanceTaskRepository;
import tech.obiteaaron.winter.embed.schedulercenter.repository.WinterJobRepository;
import tech.obiteaaron.winter.embed.schedulercenter.repository.impl.memory.WinterJobMemoryRepositoryImpl;
import tech.obiteaaron.winter.embed.schedulercenter.scheduler.WinterSchedulerExecutor;
import tech.obiteaaron.winter.embed.schedulercenter.scheduler.WinterSchedulerRegister;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public final class WinterSchedulerCenter {
    /**
     * 单例，无需多例
     */
    public static final WinterSchedulerCenter INSTANCE = new WinterSchedulerCenter();

    private static final AtomicBoolean initialized = new AtomicBoolean();

    private WinterJobRepository winterJobRepository = new WinterJobMemoryRepositoryImpl();

    private WinterJobInstanceRepository winterJobInstanceRepository;

    private WinterJobInstanceTaskRepository winterJobInstanceTaskRepository;
    /**
     * 基于RPC的实例提供集群内的多机分发功能，以实现Map、MapReduce任务功能
     */
    private WinterRpcBootstrap winterRpcBootstrap;

    private BeanParser beanParser;

    private final WinterSchedulerRegister winterSchedulerRegister = new WinterSchedulerRegister();

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
        winterSchedulerExecutor.triggerManual(winterJob, manualParams);
    }

    public void start() {
        if (!initialized.compareAndSet(false, true)) {
            return;
        }
        // 启动注册服务
        winterSchedulerRegister.setWinterJobRepository(Objects.requireNonNull(winterJobRepository, "winterJobRepository cannot be null"));
        winterSchedulerRegister.start();
        // 启动调度执行服务
        winterSchedulerExecutor.setWinterJobRepository(Objects.requireNonNull(winterJobRepository, "winterJobRepository cannot be null"));
        winterSchedulerExecutor.setWinterJobInstanceRepository(winterJobInstanceRepository);
        winterSchedulerExecutor.setWinterJobInstanceTaskRepository(winterJobInstanceTaskRepository);
        winterSchedulerExecutor.setBeanParser(Objects.requireNonNull(beanParser, "beanParser cannot be null"));
        winterSchedulerExecutor.start();
        // TODO 初始化RPC独享实例，用Map、MapReduce任务
//        winterRpcBootstrap.start();
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

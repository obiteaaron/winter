package tech.obiteaaron.winter.embed.schedulercenter;

import org.jetbrains.annotations.NotNull;
import tech.obiteaaron.winter.embed.rpc.regesiter.ConsumerConfig;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 多机分发任务处理器，分发后不合并结果
 * 执行一次后不退出，直到达成退出条件
 */
public interface MapJobProcessor extends LongTimeJobProcessor {

    @Override
    JobResult doProcessOnce(JobContext jobContext);

    @Override
    default boolean isLongTimeRunning(JobContext jobContext) {
        if (jobContext != null && Objects.equals(jobContext.getTaskType(), "MAP_SUB_TASK")) {
            // 子任务则直接退出
            return false;
        } else {
            return LongTimeJobProcessor.super.isLongTimeRunning(jobContext);
        }
    }

    /**
     * 分发子任务
     *
     * @param taskInfoList 子任务列表，用字符串表示，可以自行拼装
     */
    default void map(JobContext jobContext, List<String> taskInfoList) {
        Method processMethod = getProcessMethod();
        JobContext jobContextSub = new JobContext();
        jobContextSub.setJobId(jobContext.getJobId());
        jobContextSub.setInstanceId(jobContext.getInstanceId());
        jobContextSub.setManualParams(jobContext.getManualParams());
        // 把子任务的信息通过参数带过去
        jobContextSub.setMapTaskList(taskInfoList);
        jobContextSub.setTaskType(JobContext.TaskTypeEnum.MAP_SUB_TASK.name());

        ConsumerConfig consumerConfig = ConsumerConfig.builder()
                .interfaceName(this.getClass().getName())
                .version("1.0.0")
                .group("WinterScheduler")
                .tags(null)
                .build();
        // Map在此处只管分发，不管结果，分发成功即可。如果需要结果，可以使用Reduce任务。
        // 注意：Map任务分发最好到最细粒度，Map任务分发是同步执行返回结果，执行时请不要阻塞太久，避免RPC线程整体阻塞导致性能下降。
        Object dispatchResult = WinterSchedulerCenter.INSTANCE.getWinterRpcBootstrap().getConsumerDispatcher().dispatch(null, processMethod, new Object[]{jobContextSub}, consumerConfig);

        JobResult jobResult = (JobResult) dispatchResult;
        if (jobResult != null && jobResult.isSuccess()) {
            return;
        } else {
            throw new RuntimeException("map task dispatch failed: " + Optional.ofNullable(jobResult).map(JobResult::getMessage).orElse("result no message"));
        }
    }

    @NotNull
    default Method getProcessMethod() {
        try {
            return this.getClass().getMethod("process", JobContext.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}

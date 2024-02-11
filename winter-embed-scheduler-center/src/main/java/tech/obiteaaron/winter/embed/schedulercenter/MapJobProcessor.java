package tech.obiteaaron.winter.embed.schedulercenter;

import java.util.List;
import java.util.Objects;

/**
 * 多机分发任务处理器，分发后不合并结果
 * 执行一次后不退出，直到达成退出条件
 */
public interface MapJobProcessor extends LongTimeJobProcessor {

    @Override
    void doProcessOnce(JobContext jobContext);

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

    }
}

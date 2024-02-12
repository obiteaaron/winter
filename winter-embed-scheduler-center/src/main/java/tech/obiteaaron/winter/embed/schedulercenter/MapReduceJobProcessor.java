package tech.obiteaaron.winter.embed.schedulercenter;

import java.util.List;

/**
 * 多机分发任务处理器，分发后合并结果
 * 执行一次后不退出，直到达成退出条件
 */
public interface MapReduceJobProcessor extends MapJobProcessor {

    @Override
    JobResult doProcessOnce(JobContext jobContext);

    @Override
    default void map(JobContext jobContext, List<String> taskInfoList) {
        MapJobProcessor.super.map(jobContext, taskInfoList);
        // TODO 如果是Reduce实现存储子任务信息，子任务需要等待客户端执行结果上报以方便Reduce使用，如果不是Reduce接口，则不存储子任务
    }

    /**
     * 最终归集子任务
     *
     * @param taskInfoList 子任务列表，用字符串表示，可以自行拼装
     */
    default void reduce(JobContext jobContext, List<String> taskInfoList) {

    }
}

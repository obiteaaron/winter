package tech.obiteaaron.winter.embed.schedulercenter;

import java.util.List;

/**
 * 多机分发任务处理器，分发后合并结果
 * 执行一次后不退出，直到达成退出条件
 */
public interface MapReduceJobProcessor extends MapJobProcessor {

    @Override
    JobResult doProcessOnce(JobContext jobContext);


    /**
     * 最终归集子任务
     *
     * @param taskInfoList 子任务列表，用字符串表示，可以自行拼装
     */
    default void reduce(JobContext jobContext, List<String> taskInfoList) {

    }
}

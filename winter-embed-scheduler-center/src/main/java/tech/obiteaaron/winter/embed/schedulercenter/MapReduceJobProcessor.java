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
    default void doReduce(JobContext jobContext, JobResult jobResult) {
        if (jobContext.isMapSubTask()) {
            // 子任务不执行
            return;
        }
        // 主任务带着执行结果调用reduce方法
        reduce(jobContext, jobResult.getTaskResultList());
    }

    /**
     * 如果是Reduce则需要实现存储子任务信息，或者直接在返回值里面附加上处理结果。如果不是Reduce接口，则不存储子任务。
     * 方法1：实现者可以直接在Map的任务分发后，返回的结果里面，将处理任务的结果信息写进去，但考虑到多层Map派发任务，需要归集多层任务结果一并返回。
     * 方法2：实现者也可以自行在此保存结果，由于Map派发任务可以多层级，如果结果集较大不易处理，也直接采用业务结果，比如Mysql的明细数据，或者Redis集中存储数据。
     *
     * @param jobContext   上下文信息
     * @param taskInfoList 子任务列表，用字符串表示，可以自行拼装
     * @return
     */
    @Override
    default List<String> map(JobContext jobContext, List<String> taskInfoList) {
        return MapJobProcessor.super.map(jobContext, taskInfoList);
    }

    /**
     * 最终归集子任务
     * 方法1：如果Map后的结果不为空，则参数taskResultList是所有的结果列表，如果任务明细过多，需要注意对内存占用的量。
     * 方法2：如果Map后没有返回结果，采用直接存储的形式，则参数taskResultList为空，需自行读取存储的明细数据或集中存储中间件中的数据。
     *
     * @param taskResultList 子任务结果，用字符串表示，可以自行拼装
     */
    void reduce(JobContext jobContext, List<String> taskResultList);
}

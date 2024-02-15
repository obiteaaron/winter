package tech.obiteaaron.winter.embed.schedulercenter;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.obiteaaron.winter.common.tools.json.JsonUtils;
import tech.obiteaaron.winter.configcenter.ConfigValue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Component
@WinterScheduled(cron = "*/10 * * * * ?")
public class MapReduceJobProcessorTest implements MapReduceJobProcessor {

    @ConfigValue(name = "sleepMillisecond", group = "winter-scheduler-test")
    private int sleepMillisecond = 10_000;

    @Override
    public JobResult doProcessOnce(JobContext jobContext) {
        log.info("MapReduceJobProcessorTest process " + System.currentTimeMillis());

        if (!jobContext.isMapSubTask()) {
            // 这里应该有一个循环，分批次派发任务，最终汇聚结果。多层次派发是一样的。
            List<String> resultList = IntStream.range(1, 100).boxed().map(item -> {
                List<String> taskResultList = map(jobContext, Lists.newArrayList("item_" + item, "item_xxx"));
                return taskResultList == null ? Collections.<String>emptyList() : taskResultList;
            }).flatMap(Collection::stream).collect(Collectors.toList());

            return JobResult.success().addTaskResultList(resultList);
        } else {
            // 这里有两种做法，一种是直接附加执行结果，在内存中直接汇总，通常适合小量任务，主要取决于内存大小。另一种是返回空即可，自行存储业务结果，在reduce方法中自行查询业务结果即可。
            log.info("MapReduceJobProcessorTest subTask process " + JsonUtils.toJsonString(jobContext.getMapTaskList()));
            return JobResult.success().addTaskResultList(jobContext.getMapTaskList());
        }
    }

    @Override
    public void reduce(JobContext jobContext, List<String> taskResultList) {
        log.info("MapReduceJobProcessorTest reduce process " + JsonUtils.toJsonString(taskResultList));
    }

    @Override
    public long sleepMillisecond() {
        return sleepMillisecond;
    }
}

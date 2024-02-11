package tech.obiteaaron.winter.embed.schedulercenter;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.obiteaaron.winter.common.tools.json.JsonUtil;
import tech.obiteaaron.winter.configcenter.ConfigValue;

@Slf4j
@Component
@WinterScheduled(cron = "*/10 * * * * ?")
public class MapJobProcessorTest implements MapJobProcessor {

    @ConfigValue(name = "sleepMillisecond", group = "winter-scheduler-test")
    private int sleepMillisecond = 10_000;

    @Override
    public void doProcessOnce(JobContext jobContext) {
        log.info("MapJobProcessorTest process " + System.currentTimeMillis());

        if (!jobContext.isMapSubTask()) {
            map(jobContext, Lists.newArrayList("1", "2"));
        } else {
            log.info("MapJobProcessorTest subTask process " + JsonUtil.toJsonString(jobContext.getMapTaskList()));
        }
    }

    @Override
    public long sleepMillisecond() {
        return sleepMillisecond;
    }
}

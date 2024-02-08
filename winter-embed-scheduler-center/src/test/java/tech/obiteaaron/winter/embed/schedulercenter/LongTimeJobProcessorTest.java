package tech.obiteaaron.winter.embed.schedulercenter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.obiteaaron.winter.configcenter.ConfigValue;

@Slf4j
@Component
@WinterScheduled(cron = "*/10 * * * * ?")
public class LongTimeJobProcessorTest implements LongTimeJobProcessor {

    @ConfigValue(name = "sleepMillisecond", group = "winter-scheduler-test")
    private int sleepMillisecond = 10_000;

    @Override
    public void doProcessOnce(JobContext jobContext) {
        log.info("LongTimeJobProcessorTest process " + System.currentTimeMillis());
    }

    @Override
    public long sleepMillisecond() {
        return sleepMillisecond;
    }
}

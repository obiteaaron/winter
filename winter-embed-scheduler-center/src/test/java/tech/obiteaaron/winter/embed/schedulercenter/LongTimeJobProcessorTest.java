package tech.obiteaaron.winter.embed.schedulercenter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@WinterScheduled(cron = "*/20 * * * * ?")
public class LongTimeJobProcessorTest implements LongTimeJobProcessor {

    @Override
    public void process(JobContext jobContext) {
        log.info("LongTimeJobProcessorTest process " + System.currentTimeMillis());
    }
}

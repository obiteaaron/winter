package tech.obiteaaron.winter.embed.schedulercenter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@WinterScheduled(fixedDelay = 10_000)
public class SimpleJobProcessorTest implements SimpleJobProcessor {

    @Override
    public JobResult doProcess(JobContext jobContext) {
        log.info("SimpleJobProcessorTest process " + System.currentTimeMillis());
        return JobResult.success();
    }
}

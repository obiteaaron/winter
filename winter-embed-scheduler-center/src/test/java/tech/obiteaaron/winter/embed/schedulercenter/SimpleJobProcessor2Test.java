package tech.obiteaaron.winter.embed.schedulercenter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@WinterScheduled(fixedRate = 10_000)
public class SimpleJobProcessor2Test implements SimpleJobProcessor {

    @Override
    public JobResult doProcess(JobContext jobContext) {
        log.info("SimpleJobProcessor2Test process " + System.currentTimeMillis());
        return JobResult.success();
    }
}

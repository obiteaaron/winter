package tech.obiteaaron.winter.embed.schedulercenter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@WinterScheduled(fixedRate = 20_000)
public class SimpleJobProcessor2Test implements SimpleJobProcessor {

    @Override
    public void process(JobContext jobContext) {
        log.info("SimpleJobProcessor2Test process " + System.currentTimeMillis());
    }
}

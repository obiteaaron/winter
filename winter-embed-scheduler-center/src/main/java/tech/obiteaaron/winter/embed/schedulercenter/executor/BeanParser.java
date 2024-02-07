package tech.obiteaaron.winter.embed.schedulercenter.executor;

import tech.obiteaaron.winter.embed.schedulercenter.JobProcessor;
import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJob;

public interface BeanParser {

    JobProcessor parse(WinterJob winterJob);
}

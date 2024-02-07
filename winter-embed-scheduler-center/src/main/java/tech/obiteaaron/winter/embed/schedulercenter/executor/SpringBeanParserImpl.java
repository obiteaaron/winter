package tech.obiteaaron.winter.embed.schedulercenter.executor;

import tech.obiteaaron.winter.common.tools.spring.SpringContextHolder;
import tech.obiteaaron.winter.embed.schedulercenter.JobProcessor;
import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJob;

public class SpringBeanParserImpl implements BeanParser {
    @Override
    public JobProcessor parse(WinterJob winterJob) {
        if (winterJob.getJobProcessor() != null) {
            return winterJob.getJobProcessor();
        }
        String className = winterJob.getClassName();
        try {
            Class<?> aClass = Class.forName(className);
            return (JobProcessor) SpringContextHolder.applicationContext.getBean(aClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

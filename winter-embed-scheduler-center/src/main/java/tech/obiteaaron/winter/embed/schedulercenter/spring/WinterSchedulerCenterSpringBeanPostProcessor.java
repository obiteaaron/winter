package tech.obiteaaron.winter.embed.schedulercenter.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import tech.obiteaaron.winter.embed.schedulercenter.JobProcessor;

@Slf4j
public class WinterSchedulerCenterSpringBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof JobProcessor) {
            // TODO 加入到调度中心任务里面
//            WinterSchedulerCenter.addSchedulerJob((JobProcessor) bean);
        }
        return bean;
    }
}

package tech.obiteaaron.winter.embed.schedulercenter.timing;

import org.apache.commons.lang3.StringUtils;
import tech.obiteaaron.winter.embed.schedulercenter.JobProcessor;
import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJob;
import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJobTimeTypeEnum;
import tech.obiteaaron.winter.embed.schedulercenter.timing.strategy.CronTimeStrategyImpl;
import tech.obiteaaron.winter.embed.schedulercenter.timing.strategy.FixedDelayTimeStrategyImpl;
import tech.obiteaaron.winter.embed.schedulercenter.timing.strategy.FixedRateTimeStrategyImpl;

import java.util.Date;
import java.util.Objects;

/**
 * 处理不同的时间类型
 */
public interface TimeStrategy {

    String parseTimeExpression(WinterJob winterJob, JobProcessor jobProcessor);

    /**
     * 如果因为宕机等原因错过了执行时间，不会补全执行周期，会直接跳过，到下一次执行时间才会执行
     *
     * @param winterJob Job
     * @return 下次执行时间
     */
    Date nextTriggerTime(WinterJob winterJob);

    static TimeStrategy resolveTimeStrategy(String timeType) {
        Objects.requireNonNull(timeType, "timeType cannot be null");
        if (StringUtils.equals(timeType, WinterJobTimeTypeEnum.FIXED_DELAY.name())) {
            return new FixedDelayTimeStrategyImpl();
        }
        if (StringUtils.equals(timeType, WinterJobTimeTypeEnum.FIXED_RATE.name())) {
            return new FixedRateTimeStrategyImpl();
        }
        if (StringUtils.equals(timeType, WinterJobTimeTypeEnum.CRON.name())) {
            return new CronTimeStrategyImpl();
        }
        throw new UnsupportedOperationException("timeType resolveTimeStrategy failed " + timeType);
    }
}

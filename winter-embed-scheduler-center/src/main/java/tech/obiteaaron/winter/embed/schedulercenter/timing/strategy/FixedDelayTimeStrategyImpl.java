package tech.obiteaaron.winter.embed.schedulercenter.timing.strategy;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.ObjectUtils;
import tech.obiteaaron.winter.common.tools.json.JsonUtils;
import tech.obiteaaron.winter.embed.schedulercenter.JobProcessor;
import tech.obiteaaron.winter.embed.schedulercenter.WinterScheduled;
import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJob;
import tech.obiteaaron.winter.embed.schedulercenter.timing.TimeStrategy;

import java.util.Date;
import java.util.Map;

public class FixedDelayTimeStrategyImpl implements TimeStrategy {
    @Override
    public String parseTimeExpression(WinterJob winterJob, JobProcessor jobProcessor) {
        WinterScheduled winterScheduled = jobProcessor.getClass().getAnnotation(WinterScheduled.class);
        return JsonUtils.toJsonString(ImmutableMap.of("fixedDelay", winterScheduled.fixedDelay()));
    }

    @Override
    public Date nextTriggerTime(WinterJob winterJob) {
        Date now = new Date();
        Date beginTime = winterJob.getBeginTime();
        Date endTime = winterJob.getEndTime();
        if (beginTime != null && beginTime.after(now)) {
            // 未开始
            return beginTime;
        }
        if (endTime != null && endTime.before(now)) {
            // 已结束，直接返回null
            return null;
        }
        String timeExpression = winterJob.getTimeExpression();
        Map map = JsonUtils.parseObject(timeExpression, Map.class);
        Integer fixedDelay = (Integer) map.get("fixedDelay");
        Date baseTime = ObjectUtils.firstNonNull(winterJob.getNextTriggerTime(), now);
        Date nextTriggerTime = new Date(baseTime.getTime() + fixedDelay);
        // 如果计算结果早于当前时间则立即返回执行一次
        if (nextTriggerTime.before(now)) {
            return now;
        }
        return nextTriggerTime;
    }
}

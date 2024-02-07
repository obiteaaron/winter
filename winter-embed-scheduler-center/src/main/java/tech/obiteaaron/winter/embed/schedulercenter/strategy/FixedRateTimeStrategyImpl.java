package tech.obiteaaron.winter.embed.schedulercenter.strategy;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.ObjectUtils;
import tech.obiteaaron.winter.common.tools.json.JsonUtil;
import tech.obiteaaron.winter.embed.schedulercenter.JobProcessor;
import tech.obiteaaron.winter.embed.schedulercenter.WinterScheduled;
import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJob;

import java.util.Date;
import java.util.Map;

public class FixedRateTimeStrategyImpl implements TimeStrategy {
    @Override
    public String parseTimeExpression(WinterJob winterJob, JobProcessor jobProcessor) {
        WinterScheduled winterScheduled = jobProcessor.getClass().getAnnotation(WinterScheduled.class);
        return JsonUtil.toJsonString(ImmutableMap.of("fixedRate", winterScheduled.fixedRate()));
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
        Map map = JsonUtil.parseObject(timeExpression, Map.class);
        Integer fixedRate = (Integer) map.get("fixedRate");
        Date baseTime = ObjectUtils.firstNonNull(winterJob.getNextTriggerTime(), now);
        Date nextTriggerTime = new Date(baseTime.getTime() + fixedRate);
        // 如果计算结果早于当前时间则立即返回执行一次
        if (nextTriggerTime.before(now)) {
            return now;
        }
        return nextTriggerTime;
    }
}

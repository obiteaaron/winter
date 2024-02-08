package tech.obiteaaron.winter.embed.schedulercenter.timing.strategy;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.ObjectUtils;
import tech.obiteaaron.winter.common.tools.json.JsonUtil;
import tech.obiteaaron.winter.embed.schedulercenter.JobProcessor;
import tech.obiteaaron.winter.embed.schedulercenter.WinterScheduled;
import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJob;
import tech.obiteaaron.winter.embed.schedulercenter.timing.TimeStrategy;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

public class CronTimeStrategyImpl implements TimeStrategy {

    private final CronParser cronParser;

    public CronTimeStrategyImpl() {
        // 直接用Spring53的定义，方便兼容
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING53);
        this.cronParser = new CronParser(cronDefinition);
    }

    @Override
    public String parseTimeExpression(WinterJob winterJob, JobProcessor jobProcessor) {
        WinterScheduled winterScheduled = jobProcessor.getClass().getAnnotation(WinterScheduled.class);
        String cron = winterScheduled.cron();
        // 顺便验证一下是否支持
        Cron parse = cronParser.parse(cron);
        return JsonUtil.toJsonString(ImmutableMap.of("cron", winterScheduled.cron()));
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
        String cronString = (String) map.get("cron");
        Cron cron = cronParser.parse(cronString);
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        // 当前时间作为基准时间
        Date baseTime = ObjectUtils.firstNonNull(winterJob.getNextTriggerTime(), now);
        // 如果因为宕机等原因错过了执行时间，不会补全执行周期，会直接跳过，到下一次执行时间才会执行
        Instant instant = Instant.ofEpochMilli(baseTime.getTime());
        ZonedDateTime baseZonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        ZonedDateTime nextZonedDateTime = executionTime.nextExecution(baseZonedDateTime).orElse(null);
        if (nextZonedDateTime == null) {
            return null;
        }
        long nextTriggerTime = nextZonedDateTime.toEpochSecond() * 1000;
        if (endTime != null && endTime.getTime() < nextTriggerTime) {
            return null;
        }
        if (nextTriggerTime <= now.getTime()) {
            return now;
        }
        return new Date(nextTriggerTime);
    }
}

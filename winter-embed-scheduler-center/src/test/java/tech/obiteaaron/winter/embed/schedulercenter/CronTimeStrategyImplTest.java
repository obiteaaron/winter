package tech.obiteaaron.winter.embed.schedulercenter;

import com.google.common.collect.ImmutableMap;
import org.joda.time.DateTime;
import org.junit.Test;
import tech.obiteaaron.winter.common.tools.json.JsonUtil;
import tech.obiteaaron.winter.embed.schedulercenter.model.TimeTypeEnum;
import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJob;
import tech.obiteaaron.winter.embed.schedulercenter.strategy.CronTimeStrategyImpl;

import java.util.Date;

public class CronTimeStrategyImplTest {

    @Test
    public static void main(String[] args) {
        CronTimeStrategyImpl cronTimeStrategy = new CronTimeStrategyImpl();

        WinterJob winterJob = new WinterJob();
        winterJob.setTimeType(TimeTypeEnum.CRON.name());
        winterJob.setNextTriggerTime(new DateTime(2024, 1, 30, 0, 0, 0).toDate());
        winterJob.setTimeExpression(JsonUtil.toJsonString(ImmutableMap.of("cron", "0 0 0 1 * *")));
        Date nextTriggerTime = cronTimeStrategy.nextTriggerTime(winterJob);
        System.out.println(nextTriggerTime);
    }
}

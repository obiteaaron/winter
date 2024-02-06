package tech.obiteaaron.winter.embed.schedulercenter;

import java.lang.annotation.*;

@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WinterScheduled {

    /**
     * 秒 分 时 日 月 周
     */
    String cron() default "";

    /**
     * 固定延迟，单位毫秒
     */
    long fixedDelay() default -1;

    /**
     * 固定频率，单位毫秒。但不会启动多实例，如果一个实例未完成，下一个即便到时间也会跳过不执行。
     */
    long fixedRate() default -1;

    /**
     * 初始延迟，单位毫秒
     */
    long initialDelay() default -1;

}

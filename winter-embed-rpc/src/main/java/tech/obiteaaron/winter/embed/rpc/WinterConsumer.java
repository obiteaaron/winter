package tech.obiteaaron.winter.embed.rpc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WinterConsumer {
    /**
     * 自定义标签，可用于路由
     *
     * @return
     */
    String[] tags() default {};

    /**
     * 是否采用异步调用的方式。这通常用来突破provider执行时间过长，导致tcp、http超时而无法获取到结果的问题，日常不建议使用
     */
    boolean async() default false;

    /**
     * 仅在async=true时有效，单位：秒
     *
     * @return
     */
    int timeout() default 120;
}

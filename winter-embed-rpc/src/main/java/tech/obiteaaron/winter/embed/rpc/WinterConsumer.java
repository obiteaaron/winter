package tech.obiteaaron.winter.embed.rpc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WinterConsumer {
    /**
     * 分组
     */
    String group() default "default";

    /**
     * 版本
     */
    String version() default "1.0.0";

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
     * 仅在async=true时有效，单位：毫秒
     *
     * @return
     */
    int timeout() default 120_000;

    /**
     * 仅在async=true时有效，单位：毫秒
     * 执行时同步等待时间，比如你的安全网关是10秒超时，你可以配置为等待1~9秒，这样避免了安全网关超时，也最大限度地用同步地方式调用接口
     *
     * @return
     */
    int executeTimeout() default 3_000;

    /**
     * 仅在async=true时有效，单位：毫秒
     * 查询接口间隔时间
     *
     * @return
     */
    int asyncQueryInterval() default 10;
}

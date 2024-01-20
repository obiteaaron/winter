package tech.obiteaaron.winter.embed.rpc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface WinterProvider {
    /**
     * 指定特定接口暴露为Provider，默认不指定则：
     * 1. Class上的注解则为所有接口
     * 2. @Bean上的注解则为返回值的类型
     *
     * @return
     */
    String[] providerInterfaces() default {};

    /**
     * 自定义标签，可用于路由
     *
     * @return
     */
    String[] tags() default {};
}

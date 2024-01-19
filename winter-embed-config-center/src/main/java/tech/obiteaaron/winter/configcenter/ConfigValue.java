package tech.obiteaaron.winter.configcenter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 配置值注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigValue {
    /**
     * 配置名字
     */
    String name();

    /**
     * 配置分组
     */
    String group();

    /**
     * 配置描述，255长度
     */
    String description() default "";
}

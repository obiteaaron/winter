package tech.obiteaaron.winter;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 配置
 */
@Getter
@Setter
public final class Config {
    /**
     * 数据库主键
     */
    private Long id;
    /**
     * 配置分组（64个字符）
     */
    private String group;
    /**
     * 配置名称（64个字符）
     */
    private String name;
    /**
     * 配置内容，mediumtext长度
     */
    private String content;

    /**
     * 最后修改时间，用于拉取变化，精度毫秒
     */
    private Date lastModified;
    /**
     * 描述。长度255
     */
    private String description;

    public String uniqueKey() {
        return uniqueKey(getGroup(), getName());
    }

    public static String uniqueKey(String group, String name) {
        return group + ":" + name;
    }
}

package tech.obiteaaron.winter.configcenter;

import lombok.*;

import java.util.Date;

/**
 * 配置
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class Config {
    /**
     * 数据库主键
     */
    private Long id;

    /**
     * 创建时间，用于拉取变化，精度毫秒
     */
    private Date gmtCreate;

    /**
     * 最后修改时间，用于拉取变化，精度毫秒
     */
    private Date gmtModified;
    /**
     * 配置名称（64个字符）
     */
    private String name;
    /**
     * 配置分组（64个字符）
     */
    private String groupName;
    /**
     * 配置内容，mediumtext长度
     */
    private String content;
    /**
     * 描述。长度255
     */
    private String description;

    public String uniqueKey() {
        return uniqueKey(getGroupName(), getName());
    }

    public static String uniqueKey(String group, String name) {
        return group + ":" + name;
    }
}

package tech.obiteaaron.winter.embed.schedulercenter.repository.impl.jpa;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(indexes = {
        @Index(name = "uk_classname", columnList = "className", unique = true),
})
@Getter
@Setter
public class WinterJobDO {

    @Id
    @GeneratedValue
    private Long id;
    private Date gmtCreate;
    private Date gmtModified;

    private String name;

    private Date beginTime;

    private Date endTime;

    private Date nextTriggerTime;

    private String className;

    private String jobType;

    private String timeType;

    private String timeExpression;

    private String status;

    private String features;

    private String extraInfo;
}

package tech.obiteaaron.winter.embed.schedulercenter.repository.impl.jpa;

import lombok.Getter;
import lombok.Setter;
import tech.obiteaaron.winter.embed.schedulercenter.JobProcessor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table
@Getter
@Setter
public class WinterJobInstanceDO {

    @Id
    @GeneratedValue
    private Long id;
    private Date gmtCreate;
    private Date gmtModified;

    private Long jobId;
    private String jobName;

    private Date beginTime;

    private Date endTime;

    private String status;

    private String message;

    private String className;

    private transient JobProcessor jobProcessor;

    private Date periodTime;

    private String extraInfo;

    private String manualParams;
}

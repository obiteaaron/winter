package tech.obiteaaron.winter.embed.schedulercenter.model;

import lombok.Getter;
import lombok.Setter;
import tech.obiteaaron.winter.embed.schedulercenter.JobProcessor;

import java.util.Date;

@Getter
@Setter
public class WinterJob {

    private Long id;
    private Date gmtCreate;
    private Date gmtModified;

    private String name;
    /**
     * 开始时间
     */
    private Date beginTime;
    /**
     * 结束时间
     */
    private Date endTime;
    /**
     * 下一次执行的时间
     */
    private Date nextTriggerTime;
    /**
     * 实际执行的类名
     */
    private String className;
    /**
     * 实际执行的Bean，可选
     */
    private transient JobProcessor jobProcessor;
    /**
     * 接口名称，对应几个不同接口的实现，以最继承到最底一层的接口为准，如实现了MapReduceJobProcessor则，类型为MapReduceJobProcessor，而不是MapJobProcessor
     */
    private String jobType;

    /**
     * @see TimeTypeEnum
     */
    private String timeType;
    /**
     * 时间表达式，JSON格式
     */
    private String timeExpression;
    /**
     * NORMAL、STOPPED
     */
    private String status;
    /**
     * 内部扩展信息，JSON格式
     */
    private String features;
    /**
     * 外部自定义扩展信息，JSON格式
     */
    private String extraInfo;
}

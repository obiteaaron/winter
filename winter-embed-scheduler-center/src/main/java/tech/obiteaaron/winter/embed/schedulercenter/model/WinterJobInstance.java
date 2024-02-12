package tech.obiteaaron.winter.embed.schedulercenter.model;

import lombok.Getter;
import lombok.Setter;
import tech.obiteaaron.winter.embed.schedulercenter.JobProcessor;

import java.util.Date;

@Getter
@Setter
public class WinterJobInstance {

    private Long id;
    private Date gmtCreate;
    private Date gmtModified;

    private Long jobId;
    private String jobName;
    /**
     * 开始时间
     */
    private Date beginTime;
    /**
     * 结束时间
     */
    private Date endTime;
    /**
     * 实例状态
     */
    private String status;
    /**
     * 实例结果信息
     */
    private String message;
    /**
     * 实际执行的类名
     */
    private String className;
    /**
     * 实际执行的Bean，可选
     */
    private transient JobProcessor jobProcessor;
    /**
     * 周期执行期望时间，等同于
     *
     * @see WinterJob#getNextTriggerTime()
     */
    private Date periodTime;
    /**
     * 外部自定义扩展信息，JSON格式
     */
    private String extraInfo;
    /**
     * 手动执行时直接传入的参数，任意格式，自行传入、自行识别
     */
    private String manualParams;
}

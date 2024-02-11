package tech.obiteaaron.winter.embed.schedulercenter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobContext {

    private Long jobId;

    private Long instanceId;
    /**
     * 手动执行时直接传入的参数。任意格式，自行传入、自行识别
     */
    private String manualParams;
    /**
     * 上下文扩展信息，可以在执行过程中由使用方自行修改。任意格式，自行传入、自行识别
     */
    private String contextExtInfo;
    /**
     * NORMAL 正常任务
     * MAP_SUB_TASK MAP子任务
     *
     * @see TaskTypeEnum
     */
    private String taskType;

    public boolean isMapSubTask() {
        return TaskTypeEnum.MAP_SUB_TASK.name().equals(taskType);
    }

    public enum TaskTypeEnum {
        NORMAL, MAP_SUB_TASK
    }
}

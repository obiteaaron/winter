package tech.obiteaaron.winter.embed.schedulercenter;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobResult {

    private boolean success;

    private String message;
    /**
     * MapReduce任务时，可选：将子任务的执行结果写入，会在reduce时作为入参。也可以不写入，自行存储后，在reduce方法中自行读取存储的结果。
     */
    private List<String> taskResultList;

    public static JobResult success() {
        return new JobResult(true, null, null);
    }

    public static JobResult success(String message) {
        return new JobResult(true, message, null);
    }

    public static JobResult fail(String message) {
        return new JobResult(false, message, null);
    }

    /**
     * 追加MapReduce子任务结果
     *
     * @param taskResultList 子任务结果
     * @return
     */
    public JobResult addTaskResultList(List<String> taskResultList) {
        if (this.taskResultList == null) {
            this.taskResultList = new ArrayList<>();
        }
        if (taskResultList != null) {
            this.taskResultList.addAll(taskResultList);
        }
        return this;
    }
}

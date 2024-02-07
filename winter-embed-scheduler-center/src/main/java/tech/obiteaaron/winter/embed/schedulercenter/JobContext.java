package tech.obiteaaron.winter.embed.schedulercenter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobContext {

    private Long jobId;

    private Long instanceId;
    /**
     * 手动执行时直接传入的参数，任意格式，自行传入、自行识别
     */
    private String manualParams;
}

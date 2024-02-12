package tech.obiteaaron.winter.embed.schedulercenter;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobResult {

    private boolean success;

    private String message;

    public static JobResult success() {
        return new JobResult(true, null);
    }

    public static JobResult success(String message) {
        return new JobResult(true, message);
    }

    public static JobResult fail(String message) {
        return new JobResult(false, message);
    }
}

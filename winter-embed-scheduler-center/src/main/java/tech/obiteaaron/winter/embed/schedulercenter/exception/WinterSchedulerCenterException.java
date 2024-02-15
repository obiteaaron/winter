package tech.obiteaaron.winter.embed.schedulercenter.exception;

public class WinterSchedulerCenterException extends RuntimeException {
    public WinterSchedulerCenterException() {
        super();
    }

    public WinterSchedulerCenterException(String message) {
        super(message);
    }

    public WinterSchedulerCenterException(String message, Throwable cause) {
        super(message, cause);
    }

    public WinterSchedulerCenterException(Throwable cause) {
        super(cause);
    }

    protected WinterSchedulerCenterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

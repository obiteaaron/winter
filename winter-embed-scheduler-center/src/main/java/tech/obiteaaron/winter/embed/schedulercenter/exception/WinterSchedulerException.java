package tech.obiteaaron.winter.embed.schedulercenter.exception;

public class WinterSchedulerException extends RuntimeException {
    public WinterSchedulerException() {
        super();
    }

    public WinterSchedulerException(String message) {
        super(message);
    }

    public WinterSchedulerException(String message, Throwable cause) {
        super(message, cause);
    }

    public WinterSchedulerException(Throwable cause) {
        super(cause);
    }

    protected WinterSchedulerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

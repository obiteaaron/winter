package tech.obiteaaron.winter.strategycenter;

/**
 * 策略接口
 *
 * @param <T>
 */
public interface Strategy<T> {

    boolean support(T t);

    void execute(T t);

    default int order() {
        return 0;
    }
}

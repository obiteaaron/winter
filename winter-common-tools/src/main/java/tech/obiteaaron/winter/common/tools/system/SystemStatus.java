package tech.obiteaaron.winter.common.tools.system;

public class SystemStatus {
    /**
     * 标记系统的运行状态
     */
    public static boolean running = true;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running = false;
        }));
    }
}

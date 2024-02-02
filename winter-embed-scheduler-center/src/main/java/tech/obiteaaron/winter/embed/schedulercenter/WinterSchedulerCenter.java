package tech.obiteaaron.winter.embed.schedulercenter;

import java.util.concurrent.ConcurrentHashMap;

public final class WinterSchedulerCenter {
    /**
     * 注册进来需要执行的任务
     */
    private static final ConcurrentHashMap<String, JobProcessor> SCHEDULER_JOBS = new ConcurrentHashMap<>();

}

package tech.obiteaaron.winter.embed.schedulercenter;

/**
 * 多机分发任务处理器，分发后不合并结果
 * 执行一次后不退出，直到达成退出条件
 */
public interface MapJobProcessor extends LongTimeJobProcessor {
}

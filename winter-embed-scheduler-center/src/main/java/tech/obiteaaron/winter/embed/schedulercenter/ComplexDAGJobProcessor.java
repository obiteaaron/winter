package tech.obiteaaron.winter.embed.schedulercenter;

import java.util.List;

/**
 * 复杂的DAG(Directed acyclic graph)任务处理器，执行一次后立即退出。
 * 注意：需要确保被组合的任务能够正常退出，否则会无法进行下一步。
 */
public interface ComplexDAGJobProcessor extends JobProcessor {

    /**
     * DAG执行工作模型
     */
    class DAGJob {
        /**
         * 父级任务，可以是多个
         */
        List<JobProcessor> parentsJobList;
        /**
         * 子级任务，可以是多个
         */
        List<JobProcessor> childrenJobList;
        /**
         * 当前任务
         */
        JobProcessor current;

        public void checkCyclic() {
            throw new UnsupportedOperationException("检查到循环则抛出异常");
        }
    }
}

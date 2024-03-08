package tech.obiteaaron.winter.embed.rpc.async;

/**
 * 异步结果分布式存储，方便多机环境下共享结果
 *
 * @author nomadic
 * @date 2024/03/08
 */
public interface AsyncResultDistributeStorage {

    /**
     * 保存
     *
     * @param id    唯一幂等ID
     * @param value 业务结果
     */
    void save(String id, String value);

    /**
     * 查询
     *
     * @param id 唯一幂等ID
     * @return {@link String}
     */
    String find(String id);
}

package tech.obiteaaron.winter.embed.registercenter;

import tech.obiteaaron.winter.embed.registercenter.model.URL;

import java.util.List;

public interface RegisterService {
    /**
     * 注册生产者、消费者信息到注册中心服务
     *
     * @param registerUrl
     */
    void register(URL registerUrl);

    /**
     * 解除注册，正常情况下会解除，如果是宕机，则会由注册中心watchDog定时清理掉
     *
     * @param registerUrl
     */
    void unregister(URL registerUrl);

    /**
     * 消费者订阅回调：当生产者上线时回调；当消费者上线时回调
     *
     * @param url
     * @param listener
     */
    void subscribe(URL url, NotifyListener listener);

    /**
     * 消费者解除订阅回调，移除订阅关系
     *
     * @param url
     * @param listener
     */
    void unsubscribe(URL url, NotifyListener listener);

    /**
     * 用Consumer Url查询Provider Url列表
     *
     * @param url
     * @return
     */
    List<URL> lookup(URL url);
}

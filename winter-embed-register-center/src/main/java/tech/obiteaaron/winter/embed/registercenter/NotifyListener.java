package tech.obiteaaron.winter.embed.registercenter;

import tech.obiteaaron.winter.embed.registercenter.model.URL;

import java.util.List;

public interface NotifyListener {

    /**
     * 收到服务变化的通知时触发回调。
     *
     * @param urls 回调的URL为提供者的所有URL列表，全量，非增量
     */
    void notify(List<URL> urls);
}

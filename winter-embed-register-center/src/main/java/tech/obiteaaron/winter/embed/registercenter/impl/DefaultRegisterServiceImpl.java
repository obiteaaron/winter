package tech.obiteaaron.winter.embed.registercenter.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import tech.obiteaaron.winter.common.tools.json.JsonUtil;
import tech.obiteaaron.winter.configcenter.Config;
import tech.obiteaaron.winter.configcenter.ConfigCenter;
import tech.obiteaaron.winter.configcenter.ConfigManager;
import tech.obiteaaron.winter.embed.registercenter.NotifyListener;
import tech.obiteaaron.winter.embed.registercenter.RegisterService;
import tech.obiteaaron.winter.embed.registercenter.model.URL;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class DefaultRegisterServiceImpl implements RegisterService {

    ConfigManager configManager;

    public DefaultRegisterServiceImpl(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void register(URL url) {
        String name = parseName(url);
        String group = parseGroup(url);
        String content = parseContent(url);

        Config configQuery = new Config();
        configQuery.setName(name);
        configQuery.setGroup(group);
        List<Config> queryResult = configManager.query(configQuery);
        if (!queryResult.isEmpty() && queryResult.get(0) != null) {
            Config configOld = queryResult.get(0);
            if (configOld != null) {
                // update gmtModified
                configOld.setDescription(null);
                configOld.setContent(content);
                int modify = configManager.modify(configOld);
                Assert.isTrue(modify == 1, "register service failed");
                return;
            }
        }
        // create
        configQuery.setContent(content);
        int create = configManager.create(configQuery);
        Assert.isTrue(create == 1, "register service failed");
    }

    private String parseName(URL url) {
        return url.getPath() + ":" + url.getParameterMap().get("version");
    }

    private String parseGroup(URL url) {
        // register:default:consumer
        // register:default:provider
        return "register:" + url.getParameterMap().get("group") + ":" + url.getParameterMap().get("type");
    }

    private String parseContent(URL url) {
        return JsonUtil.toJsonString(url);
    }

    @Override
    public void unregister(URL url) {
        String a = null;
    }

    @Override
    public void subscribe(URL url, NotifyListener listener) {
        String a = null;
    }

    @Override
    public void unsubscribe(URL url, NotifyListener listener) {
        String a = null;
    }

    @Override
    public List<URL> lookup(URL url) {
        String name = parseName(url);
        String group = parseGroup(url);

        // 心跳3秒内的算有效
        long validProviderTime = System.currentTimeMillis() - 3000;
        // 直接从本地查，本地拥有全量数据
        List<Config> allConfigs = ConfigCenter.getAllConfigs();
        List<URL> collect = allConfigs.stream()
                .filter(item -> Objects.equals(item.getName(), name) && Objects.equals(item.getGroup(), group))
                .filter(item -> item.getGmtModified() != null && item.getGmtModified().getTime() > validProviderTime)
                .map(item -> JsonUtil.parseObject(item.getContent(), URL.class))
                .collect(Collectors.toList());
        return collect;
    }
}

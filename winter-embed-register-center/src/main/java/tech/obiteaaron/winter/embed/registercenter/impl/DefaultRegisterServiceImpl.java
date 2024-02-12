package tech.obiteaaron.winter.embed.registercenter.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import tech.obiteaaron.winter.common.tools.json.JsonUtils;
import tech.obiteaaron.winter.configcenter.Config;
import tech.obiteaaron.winter.configcenter.ConfigCenter;
import tech.obiteaaron.winter.configcenter.ConfigManager;
import tech.obiteaaron.winter.embed.registercenter.NotifyListener;
import tech.obiteaaron.winter.embed.registercenter.RegisterService;
import tech.obiteaaron.winter.embed.registercenter.model.URL;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@Slf4j
public class DefaultRegisterServiceImpl implements RegisterService {

    ConfigManager configManager;

    public DefaultRegisterServiceImpl(ConfigManager configManager) {
        this.configManager = configManager;
        DefaultRegisterWatchDog.INSTANCE.start(configManager);
    }

    @Override
    public void register(URL url) {
        String name = parseNameRegister(url);
        String group = parseGroup(url);
        String content = parseContent(url);

        Config configQuery = new Config();
        configQuery.setName(name);
        configQuery.setGroupName(group);
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

    private String parseNameRegister(URL url) {
        StringJoiner stringJoiner = new StringJoiner(":");
        return stringJoiner.add(url.getPath())
                .add(Objects.requireNonNull(url.getParameterMap().get("version")))
                .add(url.getIp())
                .add(String.valueOf(url.getPort()))
                .toString();
    }

    private String parseNameQuery(URL url) {
        StringJoiner stringJoiner = new StringJoiner(":");
        return stringJoiner.add(url.getPath())
                .add(Objects.requireNonNull(url.getParameterMap().get("version")))
                .toString();
    }

    private String parseGroup(URL url) {
        // register:default:consumer
        // register:default:provider
        return "register:" + url.getParameterMap().get("group") + ":" + url.getParameterMap().get("type");
    }

    private String parseContent(URL url) {
        return JsonUtils.toJsonString(url);
    }

    @Override
    public void unregister(URL url) {
        // TODO 暂时忽略，由于数据库连接池会先关闭，这里就先不做了
    }

    public void unregister0(URL url) {
        String name = parseNameRegister(url);
        String group = parseGroup(url);

        // 心跳3秒内的算有效
        long validProviderTime = System.currentTimeMillis() - 3000;
        // 直接从本地查，本地拥有全量数据
        List<Config> allConfigs = ConfigCenter.getAllConfigs();
        List<Config> unregisterUrls = allConfigs.stream()
                .filter(item -> Objects.equals(item.getGroupName(), group))
                .filter(item -> Objects.equals(item.getName(), name))
                .collect(Collectors.toList());
        doUnregister(unregisterUrls);
    }

    void doUnregister(List<Config> configs) {
        for (Config invalidUrl : configs) {
            int delete = configManager.delete(invalidUrl);
            if (delete != 1) {
                log.warn("DefaultRegisterServiceImpl delete result invalid id={}, name={}, groupName={}, result={}", invalidUrl.getId(), invalidUrl.getName(), invalidUrl.getGroupName(), delete);
            }
        }
    }

    @Override
    public void subscribe(URL url, NotifyListener listener) {
        // TODO 实现订阅功能
    }

    @Override
    public void unsubscribe(URL url, NotifyListener listener) {
        // TODO 实现下线功能
        // TODO 实现watchdog下线功能
    }

    @Override
    public List<URL> lookup(URL url) {
        String name = parseNameQuery(url);
        String group = parseGroup(url);

        // 心跳3秒内的算有效
        long validProviderTime = System.currentTimeMillis() - 3000;
        // 直接从本地查，本地拥有全量数据
        List<Config> allConfigs = ConfigCenter.getAllConfigs();
        List<URL> collect = allConfigs.stream()
                .filter(item -> Objects.equals(item.getGroupName(), group))
                .filter(item -> StringUtils.startsWith(item.getName(), name))
                .filter(item -> item.getGmtModified() != null && item.getGmtModified().getTime() > validProviderTime)
                .map(item -> JsonUtils.parseObject(item.getContent(), URL.class))
                .collect(Collectors.toList());
        return collect;
    }
}

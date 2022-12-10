package tech.obiteaaron.winter;


import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Function;

/**
 * 配置中心
 */
@Slf4j
public final class ConfigCenter {
    /**
     * 初始化的时候，如果值为null，则会写入默认值
     */
    public static final String UNDEFINED_VALUE = "UNDEFINED_VALUE";

    public static List<Config> getAllConfigs() {
        return ConfigCenterInner.getAllConfigs();
    }

    /**
     * 注册配置监听器，线程安全
     *
     * @param group    分组
     * @param name     名字
     * @param function 监听函数
     */
    public synchronized static void registerListener(String group, String name, Function<Config, Integer> function) {
        ConfigCenterInner.registerListener(group, name, function);
    }

    /**
     * 初始化配置从所有类
     *
     * @param beans                    类实例
     * @param configDatabaseRepository 配置数据库存储库
     */
    public static void initConfigFromBeans(List<Object> beans, ConfigDatabaseRepository configDatabaseRepository) {
        ConfigCenterInner.initConfigFromBeans(beans, configDatabaseRepository);
    }

    /**
     * 启动配置中心，开始自动拉取配置
     *
     * @param configDatabaseRepository 配置数据库存储库
     */
    public static void start(ConfigDatabaseRepository configDatabaseRepository) {
        ConfigCenterInner.start(configDatabaseRepository);
    }
}

package tech.obiteaaron.winter.configcenter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
final class ConfigCenterInner {
    /**
     * 内存中缓存所有配置项
     */
    private static List<Config> allConfigs = new ArrayList<>();
    /**
     * 注册监听函数，单个配置更新时会回调
     */
    private static ListValuedMap<String, Function<Config, Integer>> changedListenerFunctionMap = new ArrayListValuedHashMap<>();

    /**
     * 初始化线程池，不阻塞启动
     */
    private static final ThreadPoolExecutor INIT_THREAD_POOL = new ThreadPoolExecutor(0, 1, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));

    static List<Config> getAllConfigs() {
        return allConfigs;
    }

    /**
     * 注册配置监听器，线程安全
     *
     * @param group    分组
     * @param name     名字
     * @param function 监听函数
     */
    synchronized static void registerListener(String group, String name, Function<Config, Integer> function) {
        if (StringUtils.isAnyBlank(name, group) || function == null) {
            return;
        }
        ListValuedMap<String, Function<Config, Integer>> newMap = new ArrayListValuedHashMap<>(changedListenerFunctionMap);
        newMap.put(Config.uniqueKey(group, name), function);
        changedListenerFunctionMap = newMap;
    }

    /**
     * 注册配置监听器，整个类
     *
     * @param beans 类实例
     */
    static void initAndStart(List<Object> beans, ConfigDatabaseRepository configDatabaseRepository) {
        initConfigFromBeans(beans, configDatabaseRepository);
        start(configDatabaseRepository);
    }

    /**
     * 初始化配置从所有类
     *
     * @param beans 类实例
     */
    static void initConfigFromBeans(List<Object> beans, ConfigDatabaseRepository configDatabaseRepository) {
        if (beans == null || beans.isEmpty()) {
            return;
        }
        for (Object bean : beans) {
            if (bean == null) {
                continue;
            }
            ReflectionUtils.doWithFields(bean.getClass(), field -> {
                try {
                    ConfigValue annotation = field.getAnnotation(ConfigValue.class);
                    String name = StringUtils.firstNonBlank(annotation.name(), field.getName());
                    String group = StringUtils.firstNonBlank(annotation.group(), bean.getClass().getName());
                    String description = StringUtils.trimToEmpty(annotation.description());

                    registerListener(group, name, config -> {
                        String content = config.getContent();
                        if (content == null) {
                            return 0;
                        }
                        if (content.equals(ConfigCenter.UNDEFINED_VALUE)) {
                            return 0;
                        }
                        Object fieldValue = null;
                        field.setAccessible(true);
                        try {
                            fieldValue = field.get(bean);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                        Object newValue = ConfigValueUtil.parseValue(content, field.getType(), field.getGenericType());
                        if (newValue != null && !Objects.equals(newValue, fieldValue)) {
                            log.info("ConfigCenter config modified, group={}, name={}, content={}", group, name, content);
                            try {
                                field.set(bean, newValue);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                            return 1;
                        }
                        return 0;
                    });
                    // 第一次初始化时插入新配置
                    INIT_THREAD_POOL.submit(() -> {
                        try {
                            Long id = configDatabaseRepository.queryIdByNameAndGroup(name, group);
                            if (id != null) {
                                // 已存在
                                return;
                            }
                            field.setAccessible(true);
                            Object fieldValue = null;
                            fieldValue = field.get(bean);
                            Config config = new Config();
                            config.setName(name);
                            config.setGroupName(group);
                            config.setDescription(description);
                            String value = ConfigValueUtil.stringify(fieldValue, field.getType(), field.getGenericType());
                            config.setContent(StringUtils.firstNonBlank(value, ConfigCenter.UNDEFINED_VALUE));
                            configDatabaseRepository.create(config);
                        } catch (Exception e) {
                            log.error("create configValue exception name={}, group={}", name, group, e);
                        }
                    });

                } catch (Throwable t) {
                    log.error("ConfigCenter register bean exception", t);
                }
            }, field -> {
                ConfigValue annotation = field.getAnnotation(ConfigValue.class);
                return annotation != null;
            });
        }
    }

    /**
     * 启动配置中心，开始自动拉取配置
     */
    static void start(ConfigDatabaseRepository configDatabaseRepository) {
        log.info("ConfigCenter starting...");
        ConfigCenterPullTask configCenterPullTask = new ConfigCenterPullTask();
        configCenterPullTask.setConfigDatabaseRepository(configDatabaseRepository);
        configCenterPullTask.autoPull();

        log.info("ConfigCenter started.");
    }

    /**
     * 合并增量配置并触发监听器
     *
     * @param deltaConfigs 增量配置
     * @return int 变化的配置数量
     */
    static int mergeDeltaConfigAndTriggerListener(List<Config> deltaConfigs) {
        // 合并增量配置
        mergeDeltaConfig(deltaConfigs);
        // 触发全部监听器
        @SuppressWarnings("UnnecessaryLocalVariable")
        int effectNum = triggerListener();
        return effectNum;
    }

    private static void mergeDeltaConfig(List<Config> deltaConfigs) {
        if (deltaConfigs == null || deltaConfigs.isEmpty()) {
            return;
        }
        Map<String, Config> map = allConfigs.stream().collect(Collectors.toMap(Config::uniqueKey, item -> item));
        for (Config deltaConfig : deltaConfigs) {
            map.put(deltaConfig.uniqueKey(), deltaConfig);
        }
        allConfigs = new ArrayList<>(map.values());
    }

    private static int triggerListener() {
        if (allConfigs == null || allConfigs.isEmpty()) {
            return 0;
        }
        @SuppressWarnings("UnnecessaryLocalVariable")
        int effectNum = allConfigs.stream().map(item -> {

            List<Function<Config, Integer>> functions = changedListenerFunctionMap.get(item.uniqueKey());
            if (functions != null && !functions.isEmpty()) {
                for (Function<Config, Integer> function : functions) {
                    try {
                        Integer apply = function.apply(item);
                        return apply == null ? 0 : apply;
                    } catch (Throwable t) {
                        log.error("ConfigCenter listener invoke exception, group={}, name={}", item.getGroupName(), item.getName(), t);
                    }
                }
            }
            return 0;
        }).mapToInt(item -> item).sum();
        return effectNum;
    }


}

package tech.obiteaaron.winter.configcenter;

import lombok.Setter;

import java.util.List;

public final class ConfigManager {

    @Setter
    private ConfigDatabaseRepository configDatabaseRepository;

    public List<Config> query(Config config) {
        return configDatabaseRepository.query(config);
    }

    public int create(Config config) {
        return configDatabaseRepository.create(config);
    }

    public int modify(Config config) {
        return configDatabaseRepository.modify(config);
    }

    /**
     * 物理删除，注意数据安全
     * @param config 需要删除的配置
     * @return 删除数量
     */
    public int delete(Config config) {
        return configDatabaseRepository.delete(config);
    }
}

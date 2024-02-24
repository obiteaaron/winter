package tech.obiteaaron.winter.configcenter.service.impl;

import lombok.Setter;
import tech.obiteaaron.winter.configcenter.Config;
import tech.obiteaaron.winter.configcenter.repository.ConfigDatabaseRepository;
import tech.obiteaaron.winter.configcenter.service.ConfigManagerService;

import java.util.List;

public final class ConfigManagerServiceImpl implements ConfigManagerService {

    @Setter
    private ConfigDatabaseRepository configDatabaseRepository;

    @Override
    public List<Config> query(Config config) {
        return configDatabaseRepository.query(config);
    }

    @Override
    public int create(Config config) {
        return configDatabaseRepository.create(config);
    }

    @Override
    public int modify(Config config) {
        return configDatabaseRepository.modify(config);
    }

    /**
     * 物理删除，注意数据安全
     *
     * @param config 需要删除的配置
     * @return 删除数量
     */
    @Override
    public int delete(Config config) {
        return configDatabaseRepository.delete(config);
    }
}

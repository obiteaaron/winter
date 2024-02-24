package tech.obiteaaron.winter.configcenter.service;

import tech.obiteaaron.winter.configcenter.Config;
import tech.obiteaaron.winter.configcenter.repository.ConfigDatabaseRepository;

import java.util.List;

/**
 * 配置管理服务
 */
public interface ConfigManagerService {

    List<Config> query(Config config);

    int create(Config config);

    int modify(Config config);

    /**
     * 物理删除，注意数据安全
     *
     * @param config 需要删除的配置
     * @return 删除数量
     */
    int delete(Config config);

    void setConfigDatabaseRepository(ConfigDatabaseRepository configDatabaseRepository);
}

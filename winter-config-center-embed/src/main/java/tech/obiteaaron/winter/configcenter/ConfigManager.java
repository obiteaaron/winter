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
}

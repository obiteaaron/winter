package tech.obiteaaron.winter;

import lombok.Setter;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;

final class ConfigDatabaseRepository {
    @Setter
    private DataSource dataSource;

    public List<Config> query(Config config) {
        return null;
    }

    public int create(Config config) {
        return 0;
    }

    public int modify(Config config) {
        return 0;
    }

    public List<Config> queryAll() {
        return null;
    }

    public List<Config> queryDelta(Date lastPullDate) {
        return null;
    }

    public Long queryIdByNameAndGroup(String name, String group) {
        return null;
    }
}

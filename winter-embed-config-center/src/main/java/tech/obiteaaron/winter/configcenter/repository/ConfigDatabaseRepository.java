package tech.obiteaaron.winter.configcenter.repository;

import tech.obiteaaron.winter.configcenter.Config;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;

/**
 * 存储管理器，未来增加扩展点，可以直接替换为其他存储实现。
 * 如有必要，也可以采用手动初始化的方式进行替换。
 */
public interface ConfigDatabaseRepository {

    List<Config> query(Config config);

    int create(Config config);

    int modify(Config config);

    int delete(Config config);

    List<Config> queryAll();

    List<Config> queryDelta(Date lastPullDate);

    Long queryIdByNameAndGroup(String name, String group);

    void setDataSource(DataSource dataSource);
}

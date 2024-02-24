package tech.obiteaaron.winter.configcenter.repository.impl;

import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import tech.obiteaaron.winter.configcenter.Config;
import tech.obiteaaron.winter.configcenter.repository.ConfigDatabaseRepository;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 存储管理器，未来增加扩展点，可以直接替换为其他存储实现。
 * 如有必要，也可以采用手动初始化的方式进行替换。
 */
@SuppressWarnings({"SqlNoDataSourceInspection", "SqlDialectInspection"})
public class ConfigDatabaseRepositoryMysqlImpl implements ConfigDatabaseRepository {

    @Setter
    private DataSource dataSource;

    private volatile JdbcTemplate jdbcTemplate;

    @Override
    public List<Config> query(Config config) {
        Objects.requireNonNull(config);
        String group = StringUtils.trimToEmpty(config.getGroupName());
        String name = StringUtils.trimToEmpty(config.getName());
        String sql = "select `id`, `name`, `group_name` as groupName, `content`, `gmt_create` as gmtCreate, `gmt_modified` as gmtModified, `description` from winter_embed_config_center where `name` like ? and `group_name` = ? order by gmt_modified desc";
        return jdbcTemplate().query(sql, new Object[]{'%' + name + '%', group}, new int[]{Types.VARCHAR, Types.VARCHAR}, new BeanPropertyRowMapper<>(Config.class));
    }

    @Override
    public int create(Config config) {
        Objects.requireNonNull(config);
        String name = Objects.requireNonNull(StringUtils.trimToNull(config.getName()));
        String group = Objects.requireNonNull(StringUtils.trimToNull(config.getGroupName()));
        String content = Objects.requireNonNull(StringUtils.trimToNull(config.getContent()));
        String description = StringUtils.trimToEmpty(config.getDescription());

        String sql = "insert into  winter_embed_config_center(`name`, `group_name`, `content`, `description`) values (?, ?, ?, ?)";
        return jdbcTemplate().update(sql, name, group, content, description);

    }

    @Override
    public int modify(Config config) {
        Objects.requireNonNull(config);
        Long id = Objects.requireNonNull(config.getId());
        String content = Objects.requireNonNull(StringUtils.trimToNull(config.getContent()));
        String group = Objects.requireNonNull(StringUtils.trimToNull(config.getGroupName()));
        String name = Objects.requireNonNull(StringUtils.trimToNull(config.getName()));
        String description = StringUtils.trimToNull(config.getDescription());
        if (description == null) {
            String sql = "update winter_embed_config_center set `content` = ?, `gmt_modified` = current_timestamp(3) where `id` = ? and `name` = ? and `group_name` = ?";
            return jdbcTemplate().update(sql, content, id, name, group);
        } else {
            String sql = "update winter_embed_config_center set `content` = ?, `gmt_modified` = current_timestamp(3), `description` = ? where `id` = ? and `name` = ? and `group_name` = ?";
            return jdbcTemplate().update(sql, content, description, id, name, group);
        }
    }

    @Override
    public int delete(Config config) {
        Objects.requireNonNull(config);
        Long id = Objects.requireNonNull(config.getId());
        String group = Objects.requireNonNull(StringUtils.trimToNull(config.getGroupName()));
        String name = Objects.requireNonNull(StringUtils.trimToNull(config.getName()));

        String sql = "delete from winter_embed_config_center where `id` = ? and `name` = ? and `group_name` = ?";
        return jdbcTemplate().update(sql, id, name, group);
    }

    @Override
    public List<Config> queryAll() {
        String sql = "select `id`, `name`, `group_name` as groupName, `content`, `gmt_create` as gmtCreate, `gmt_modified` as gmtModified from winter_embed_config_center order by gmt_modified asc";
        return jdbcTemplate().query(sql, new BeanPropertyRowMapper<>(Config.class));
    }

    @Override
    public List<Config> queryDelta(Date lastPullDate) {
        String sql = "select `id`, `name`, `group_name` as groupName, `content`, `gmt_create` as gmtCreate, `gmt_modified` as gmtModified from winter_embed_config_center where gmt_modified > ? order by gmt_modified asc";
        return jdbcTemplate().query(sql, new Object[]{lastPullDate}, new int[]{Types.TIMESTAMP}, new BeanPropertyRowMapper<>(Config.class));
    }

    @Override
    public Long queryIdByNameAndGroup(String name, String group) {
        String sql = "select `id` from winter_embed_config_center where `name` = ? and `group_name` = ? limit 1";
        List<Long> longs = jdbcTemplate().query(sql, new Object[]{name, group}, new int[]{Types.VARCHAR, Types.VARCHAR}, SingleColumnRowMapper.newInstance(Long.class));
        return DataAccessUtils.singleResult(longs);
    }

    private synchronized JdbcTemplate jdbcTemplate() {
        if (jdbcTemplate == null) {
            jdbcTemplate = new JdbcTemplate(dataSource);
        }
        return jdbcTemplate;
    }
}

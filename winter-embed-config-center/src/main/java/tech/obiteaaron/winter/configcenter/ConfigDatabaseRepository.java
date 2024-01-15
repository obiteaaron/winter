package tech.obiteaaron.winter.configcenter;

import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@SuppressWarnings({"SqlNoDataSourceInspection", "SqlDialectInspection"})
final class ConfigDatabaseRepository {

    @Setter
    private DataSource dataSource;

    private volatile JdbcTemplate jdbcTemplate;

    public List<Config> query(Config config) {
        Objects.requireNonNull(config);
        String group = StringUtils.trimToEmpty(config.getGroup());
        String name = StringUtils.trimToEmpty(config.getName());
        String sql = "select `id`, `name`, `group`, `content`, `last_modified` as lastModified, `description` from winter_config_center where `name` like ? and `group` = ? order by last_modified desc";
        return jdbcTemplate().query(sql, new Object[]{'%' + name + '%', group}, new int[]{Types.VARCHAR, Types.VARCHAR}, new BeanPropertyRowMapper<>(Config.class));
    }

    public int create(Config config) {
        Objects.requireNonNull(config);
        String name = Objects.requireNonNull(StringUtils.trimToNull(config.getName()));
        String group = Objects.requireNonNull(StringUtils.trimToNull(config.getGroup()));
        String content = Objects.requireNonNull(StringUtils.trimToNull(config.getContent()));
        String description = StringUtils.trimToEmpty(config.getDescription());

        String sql = "insert into  winter_config_center(`name`, `group`, `content`, `description`) values (?, ?, ?, ?)";
        return jdbcTemplate().update(sql, name, group, content, description);

    }

    public int modify(Config config) {
        Objects.requireNonNull(config);
        Long id = Objects.requireNonNull(config.getId());
        String content = Objects.requireNonNull(StringUtils.trimToNull(config.getContent()));
        String group = Objects.requireNonNull(StringUtils.trimToNull(config.getGroup()));
        String name = Objects.requireNonNull(StringUtils.trimToNull(config.getName()));
        String description = StringUtils.trimToNull(config.getDescription());
        if (description == null) {
            String sql = "update winter_config_center set `content` = ?, `last_modified` = current_timestamp(3) where `id` = ? and `name` = ? and `group` = ?";
            return jdbcTemplate().update(sql, content, id, name, group);
        } else {
            String sql = "update winter_config_center set `content` = ?, `last_modified` = current_timestamp(3), `description` = ? where `id` = ? and `name` = ? and `group` = ?";
            return jdbcTemplate().update(sql, content, description, id, name, group);
        }
    }

    public List<Config> queryAll() {
        String sql = "select `id`, `name`, `group`, `content`, `last_modified` as lastModified from winter_config_center order by last_modified asc";
        return jdbcTemplate().query(sql, new BeanPropertyRowMapper<>(Config.class));
    }

    public List<Config> queryDelta(Date lastPullDate) {
        String sql = "select `id`, `name`, `group`, `content`, `last_modified` as lastModified from winter_config_center where last_modified > ? order by last_modified asc";
        return jdbcTemplate().query(sql, new Object[]{lastPullDate}, new int[]{Types.TIMESTAMP}, new BeanPropertyRowMapper<>(Config.class));
    }

    public Long queryIdByNameAndGroup(String name, String group) {
        String sql = "select `id` from winter_config_center where `name` = ? and `group` = ? limit 1";
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

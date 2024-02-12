package tech.obiteaaron.winter.embed.schedulercenter.repository.impl.mysql;

import lombok.Setter;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public class JdbcUtils {

    @Setter
    private DataSource dataSource;

    private volatile JdbcTemplate jdbcTemplate;

    public JdbcTemplate jdbcTemplate() {
        if (jdbcTemplate == null) {
            synchronized (this) {
                if (jdbcTemplate == null) {
                    jdbcTemplate = new JdbcTemplate(dataSource);
                }
            }
        }
        return jdbcTemplate;
    }
}

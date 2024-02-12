package tech.obiteaaron.winter.embed.schedulercenter.repository.impl.mysql;

import lombok.Setter;
import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJob;
import tech.obiteaaron.winter.embed.schedulercenter.repository.WinterJobRepository;
import tech.obiteaaron.winter.embed.schedulercenter.repository.request.WinterJobQuery;

import java.util.List;

public class WinterJobMysqlRepositoryImpl implements WinterJobRepository {

    @Setter
    private JdbcUtils jdbcUtils;

    @Override
    public boolean save(WinterJob winterJob) {
        // TODO
        return false;
    }

    @Override
    public List<WinterJob> queryAll(WinterJobQuery winterJobQuery) {
        // TODO
        return null;
    }
}

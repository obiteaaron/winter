package tech.obiteaaron.winter.embed.schedulercenter.repository.impl.mysql;

import lombok.Setter;
import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJobInstance;
import tech.obiteaaron.winter.embed.schedulercenter.repository.WinterJobInstanceRepository;
import tech.obiteaaron.winter.embed.schedulercenter.repository.request.WinterJobInstanceQuery;

public class WinterJobInstanceMysqlRepositoryImpl implements WinterJobInstanceRepository {

    @Setter
    private JdbcUtils jdbcUtils;

    @Override
    public boolean save(WinterJobInstance winterJobInstance) {
        // TODO
        return false;
    }

    @Override
    public WinterJobInstance queryById(long instanceId) {
        return null;
    }

    @Override
    public void queryAll(WinterJobInstanceQuery winterJobInstanceQuery) {

    }
}

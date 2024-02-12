package tech.obiteaaron.winter.embed.schedulercenter.repository.impl.jpa;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJobInstance;
import tech.obiteaaron.winter.embed.schedulercenter.repository.WinterJobInstanceRepository;
import tech.obiteaaron.winter.embed.schedulercenter.repository.request.WinterJobInstanceQuery;

import java.util.Optional;

public class WinterJobInstanceJpaRepositoryImpl implements WinterJobInstanceRepository {

    @Autowired
    private WinterJobInstanceJpaRepository winterJobInstanceJpaRepository;

    @Override
    public boolean save(WinterJobInstance winterJobInstance) {
        WinterJobInstanceDO winterJobInstanceDO = new WinterJobInstanceDO();
        BeanUtils.copyProperties(winterJobInstance, winterJobInstanceDO);
        WinterJobInstanceDO save = winterJobInstanceJpaRepository.save(winterJobInstanceDO);
        winterJobInstance.setId(save.getId());
        return true;
    }

    @Override
    public WinterJobInstance queryById(long instanceId) {
        Optional<WinterJobInstanceDO> byId = winterJobInstanceJpaRepository.findById(instanceId);

        return byId.map(item -> {
            WinterJobInstance winterJobInstance = new WinterJobInstance();
            BeanUtils.copyProperties(item, winterJobInstance);
            return winterJobInstance;
        }).orElse(null);
    }

    @Override
    public void queryAll(WinterJobInstanceQuery winterJobInstanceQuery) {

    }
}

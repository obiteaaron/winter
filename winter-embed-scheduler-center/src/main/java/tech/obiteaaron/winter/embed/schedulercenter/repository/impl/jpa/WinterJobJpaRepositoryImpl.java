package tech.obiteaaron.winter.embed.schedulercenter.repository.impl.jpa;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJob;
import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJobStatusEnum;
import tech.obiteaaron.winter.embed.schedulercenter.repository.WinterJobRepository;
import tech.obiteaaron.winter.embed.schedulercenter.repository.request.WinterJobQuery;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WinterJobJpaRepositoryImpl implements WinterJobRepository {

    @Autowired
    private WinterJobJpaRepository winterJobJpaRepository;

    @Override
    public boolean save(WinterJob winterJob) {
        WinterJobDO winterJobDO = new WinterJobDO();
        BeanUtils.copyProperties(winterJob, winterJobDO);
        if (winterJob.getId() == null) {
            // 创建的情况下，判断一下，避免创建重复实例
            Optional<WinterJobDO> byClassName = winterJobJpaRepository.findByClassName(winterJobDO.getClassName());
            if (byClassName.isPresent()) {
                winterJob.setId(byClassName.get().getId());
                return true;
            }
        }
        WinterJobDO save = winterJobJpaRepository.save(winterJobDO);
        winterJob.setId(save.getId());
        return true;
    }

    @Override
    public List<WinterJob> queryAll(WinterJobQuery winterJobQuery) {
        boolean onlyNeedExecuting = winterJobQuery.isOnlyNeedExecuting();
        List<WinterJobDO> all;
        if (onlyNeedExecuting) {
            all = winterJobJpaRepository.findAllByStatusAndNextTriggerTimeLessThan(WinterJobStatusEnum.NORMAL.name(), new Date(System.currentTimeMillis() + winterJobQuery.getTimeDeviationSecond() * 2 * 1000L));
        } else {
            all = winterJobJpaRepository.findAll();
        }

        return all.stream()
                .map(item -> {
                    WinterJob winterJob = new WinterJob();
                    BeanUtils.copyProperties(item, winterJob);
                    return winterJob;
                })
                .collect(Collectors.toList());
    }
}

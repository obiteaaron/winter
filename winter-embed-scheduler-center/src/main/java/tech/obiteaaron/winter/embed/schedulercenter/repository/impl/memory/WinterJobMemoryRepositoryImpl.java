package tech.obiteaaron.winter.embed.schedulercenter.repository.impl.memory;

import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJob;
import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJonStatusEnum;
import tech.obiteaaron.winter.embed.schedulercenter.repository.WinterJobRepository;
import tech.obiteaaron.winter.embed.schedulercenter.repository.request.WinterJobQuery;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class WinterJobMemoryRepositoryImpl implements WinterJobRepository {

    private final ConcurrentHashMap<Long, WinterJob> winterJobMap = new ConcurrentHashMap<>();

    @Override
    public boolean save(WinterJob winterJob) {
        winterJobMap.put(winterJob.getId(), winterJob);
        return true;
    }

    @Override
    public List<WinterJob> queryAll(WinterJobQuery winterJobQuery) {
        return winterJobMap.values().stream()
                .filter(item -> !winterJobQuery.isOnlyNeedExecuting() || Objects.equals(WinterJonStatusEnum.NORMAL.name(), item.getStatus()))
                .collect(Collectors.toList());
    }
}

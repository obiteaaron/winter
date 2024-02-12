package tech.obiteaaron.winter.embed.schedulercenter.repository.impl.memory;

import tech.obiteaaron.winter.common.tools.id.TimestampGenerator;
import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJobInstance;
import tech.obiteaaron.winter.embed.schedulercenter.repository.WinterJobInstanceRepository;
import tech.obiteaaron.winter.embed.schedulercenter.repository.request.WinterJobInstanceQuery;

import java.util.concurrent.ConcurrentHashMap;

public class WinterJobInstanceMemoryRepositoryImpl implements WinterJobInstanceRepository {

    private final ConcurrentHashMap<Long, WinterJobInstance> winterJobInstanceMap = new ConcurrentHashMap<>();

    private final TimestampGenerator timestampGenerator = new TimestampGenerator();

    @Override
    public boolean save(WinterJobInstance winterJobInstance) {
        if (winterJobInstance.getId() == null) {
            winterJobInstance.setId(timestampGenerator.generate());
        }
        winterJobInstanceMap.put(winterJobInstance.getId(), winterJobInstance);
        return true;
    }

    @Override
    public WinterJobInstance queryById(long instanceId) {
        return winterJobInstanceMap.get(instanceId);
    }

    @Override
    public void queryAll(WinterJobInstanceQuery winterJobInstanceQuery) {

    }
}

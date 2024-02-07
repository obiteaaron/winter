package tech.obiteaaron.winter.embed.schedulercenter.repository;

import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJobInstance;
import tech.obiteaaron.winter.embed.schedulercenter.repository.request.WinterJobInstanceQuery;

public interface WinterJobInstanceRepository {

    boolean save(WinterJobInstance winterJobInstance);

    WinterJobInstance queryById(long instanceId);

    void queryAll(WinterJobInstanceQuery winterJobInstanceQuery);
}

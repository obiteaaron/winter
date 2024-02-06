package tech.obiteaaron.winter.embed.schedulercenter.repository;

import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJob;
import tech.obiteaaron.winter.embed.schedulercenter.repository.request.WinterJobQuery;

public interface WinterJobInstanceRepository {

    void save(WinterJob winterJob);

    void queryAll(WinterJobQuery winterJobQuery);
}

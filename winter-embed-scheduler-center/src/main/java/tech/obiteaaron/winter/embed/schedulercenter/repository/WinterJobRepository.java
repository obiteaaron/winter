package tech.obiteaaron.winter.embed.schedulercenter.repository;

import tech.obiteaaron.winter.embed.schedulercenter.model.WinterJob;
import tech.obiteaaron.winter.embed.schedulercenter.repository.request.WinterJobQuery;

import java.util.List;

public interface WinterJobRepository {

    boolean save(WinterJob winterJob);

    List<WinterJob> queryAll(WinterJobQuery winterJobQuery);
}

package tech.obiteaaron.winter.embed.schedulercenter.repository.impl.jpa;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WinterJobJpaQuery {
    /**
     * 是否只查需要执行的
     */
    boolean onlyNeedExecuting;
    /**
     * 时间偏差多少秒
     */
    int timeDeviationSecond;
}

package tech.obiteaaron.winter.embed.schedulercenter.repository.impl.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface WinterJobJpaRepository extends JpaRepository<WinterJobDO, Long> {

    List<WinterJobDO> findAllByStatusAndNextTriggerTimeLessThan(String status, Date timeDeviation);

    Optional<WinterJobDO> findByClassName(String className);
}

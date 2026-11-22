package onl.tesseract.srp.job.adapter.serverside.jpa;

import onl.tesseract.srp.job.adapter.serverside.jpa.entity.JobPlayerProgressionEntityKey;
import onl.tesseract.srp.job.adapter.serverside.jpa.repository.JobPlayerProgressionJpaRepository;
import onl.tesseract.srp.job.domain.model.JobName;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.job.domain.model.PlayerJobProgression;
import onl.tesseract.srp.job.domain.port.serverside.JobPlayerProgressionRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class JpaJobPlayerProgressionRepository implements JobPlayerProgressionRepository {

    final private JobPlayerProgressionJpaRepository jobPlayerProgressionJpaRepository;

    public JpaJobPlayerProgressionRepository(JobPlayerProgressionJpaRepository jobPlayerProgressionJpaRepository) {
        this.jobPlayerProgressionJpaRepository = jobPlayerProgressionJpaRepository;
    }

    @Override
    @Cacheable(cacheNames = "playerJobProgression")
    public PlayerJobProgression getPlayerJobProgression(PlayerId player, JobName jobName) {
        return jobPlayerProgressionJpaRepository.findById(new JobPlayerProgressionEntityKey(player.value(), jobName.value()))
                .map(JobPlayerProgressionMapper::toDomain)
                .orElse(new PlayerJobProgression(jobName));
    }
}

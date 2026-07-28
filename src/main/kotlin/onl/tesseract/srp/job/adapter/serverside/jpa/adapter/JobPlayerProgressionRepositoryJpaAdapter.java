package onl.tesseract.srp.job.adapter.serverside.jpa.adapter;

import onl.tesseract.srp.job.adapter.serverside.jpa.entity.JobPlayerProgressionEntity;
import onl.tesseract.srp.job.adapter.serverside.jpa.entity.JobPlayerProgressionEntityKey;
import onl.tesseract.srp.job.adapter.serverside.jpa.entity.JobPlayerTalentProgressionEntity;
import onl.tesseract.srp.job.adapter.serverside.jpa.repository.JobPlayerProgressionJpaRepository;
import onl.tesseract.srp.job.domain.model.JobName;
import onl.tesseract.srp.job.domain.model.PlayerID;
import onl.tesseract.srp.job.domain.model.PlayerJobProgression;
import onl.tesseract.srp.job.domain.model.talent.TalentName;
import onl.tesseract.srp.job.domain.port.serverside.JobPlayerProgressionRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class JobPlayerProgressionRepositoryJpaAdapter implements JobPlayerProgressionRepository {

    final private JobPlayerProgressionJpaRepository jobPlayerProgressionJpaRepository;

    public JobPlayerProgressionRepositoryJpaAdapter(JobPlayerProgressionJpaRepository jobPlayerProgressionJpaRepository) {
        this.jobPlayerProgressionJpaRepository = jobPlayerProgressionJpaRepository;
    }

    @Override
    @Cacheable(cacheNames = "playerJobProgression")
    public PlayerJobProgression getPlayerJobProgression(PlayerID player, JobName jobName) {
        return jobPlayerProgressionJpaRepository.findById(new JobPlayerProgressionEntityKey(player.value(), jobName.value()))
                .map(this::toDomainModel)
                .orElse(new PlayerJobProgression(jobName));
    }

    private PlayerJobProgression toDomainModel(JobPlayerProgressionEntity jobPlayerProgressionEntity) {
        return new PlayerJobProgression(
                new JobName(jobPlayerProgressionEntity.getId().getJobName()),
                new PlayerJobProgression.TalentProgressions(jobPlayerProgressionEntity.getTalentProgressions().stream()
                        .collect(Collectors.toMap(it->
                                new TalentName(it.getTalentName()),
                                JobPlayerTalentProgressionEntity::getLevel
                        ))),
                jobPlayerProgressionEntity.getAvailableTalentPoint(),
                jobPlayerProgressionEntity.getTotalTalentPoint()
        );
    }

}

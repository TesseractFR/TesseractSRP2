package onl.tesseract.srp.job.adapter.serverside.jpa.adapter;

import onl.tesseract.srp.job.adapter.serverside.jpa.entity.JobMissionEntity;
import onl.tesseract.srp.job.adapter.serverside.jpa.repository.JobMissionJpaRepository;
import onl.tesseract.srp.job.domain.model.mission.JobMission;
import onl.tesseract.srp.job.domain.port.serverside.JobMissionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * JPA adapter for JobMissionRepository.
 */
@Component
public class JobMissionRepositoryJpaAdapter implements JobMissionRepository {
    private final JobMissionJpaRepository jpaRepo;

    public JobMissionRepositoryJpaAdapter(JobMissionJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public List<JobMission> findAllByPlayerId(UUID playerId) {
        return jpaRepo.findAllByPlayerId(playerId).stream()
                .map(JobMissionEntity::toDomain)
                .toList();
    }

    public JobMission save(JobMission entity) {
        JobMissionEntity saved = jpaRepo.save(JobMissionEntity.fromDomain(entity));
        return saved.toDomain();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepo.deleteById(id);
    }
}


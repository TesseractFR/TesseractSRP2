package onl.tesseract.srp.adapter.serverside.jpa.adapter;

import onl.tesseract.srp.adapter.serverside.jpa.entity.JobMissionEntity;
import onl.tesseract.srp.adapter.serverside.jpa.repository.JobMissionJpaRepository;
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
    public JobMission getById(Long id) {
        return jpaRepo.findById(id).map(JobMissionEntity::toDomain).orElse(null);
    }

    @Override
    public List<JobMission> findAllByPlayerId(UUID playerId) {
        return jpaRepo.findAllByPlayerId(playerId).stream()
                .map(JobMissionEntity::toDomain)
                .toList();
    }

    @Override
    public JobMission save(JobMission entity) {
        JobMissionEntity saved = jpaRepo.save(JobMissionEntity.fromDomain(entity));
        return saved.toDomain();
    }

    @Override
    public Long idOf(JobMission entity) {
        return entity.id();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepo.deleteById(id);
    }
}


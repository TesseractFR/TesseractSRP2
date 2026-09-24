package onl.tesseract.srp.adapter.serverside.jpa.repository;

import onl.tesseract.srp.adapter.serverside.jpa.entity.JobMissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * JPA repository interface for JobMissionEntity.
 */
@org.springframework.stereotype.Repository
public interface JobMissionJpaRepository extends JpaRepository<JobMissionEntity, Long> {
    List<JobMissionEntity> findAllByPlayerId(UUID playerId);
}

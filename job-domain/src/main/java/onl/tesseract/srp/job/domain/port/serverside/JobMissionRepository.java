package onl.tesseract.srp.job.domain.port.serverside;

import onl.tesseract.lib.repository.Repository;
import onl.tesseract.srp.job.domain.model.mission.JobMission;
import java.util.List;
import java.util.UUID;

public interface JobMissionRepository extends Repository<JobMission, Long> {
    void deleteById(Long id);
    List<JobMission> findAllByPlayerId(UUID playerId);
}


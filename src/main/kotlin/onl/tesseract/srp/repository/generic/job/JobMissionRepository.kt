package onl.tesseract.srp.repository.generic.job

import onl.tesseract.lib.repository.Repository
import onl.tesseract.srp.domain.job.mission.JobMission
import java.util.UUID

interface JobMissionRepository : Repository<JobMission, Long> {
    fun deleteById(id: Long)
    fun findAllByPlayerId(playerId: UUID): List<JobMission>
}

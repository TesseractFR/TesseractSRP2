package onl.tesseract.srp.repository.hibernate.job.mission

import onl.tesseract.lib.repository.Repository
import onl.tesseract.srp.domain.job.mission.JobMission
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import java.util.*

interface JobMissionRepository : Repository<JobMission, Long> {
    fun deleteById(id: Long)
    fun findAll(): List<JobMission>
    fun findAllByPlayerId(playerId: UUID): List<JobMission>
}

@Component
class JobMissionRepositoryJpaAdapter(
    private val jpaRepo: JobMissionJpaRepository
) : JobMissionRepository {

    override fun getById(id: Long): JobMission? {
        return jpaRepo.findById(id)
            .orElse(null)
            ?.toDomain()
    }

    override fun findAllByPlayerId(playerId: UUID): List<JobMission> {
        return jpaRepo.findAllByPlayerId(playerId).map { it.toDomain() }
    }

    override fun save(entity: JobMission) {
        jpaRepo.save(entity.toEntity())
    }

    override fun idOf(entity: JobMission): Long {
        return entity.id ?: error("JobMission ID is null")
    }

    override fun deleteById(id: Long) {
        jpaRepo.deleteById(id)
    }

    override fun findAll(): List<JobMission> {
        return jpaRepo.findAll().map { it.toDomain() }
    }

}

@org.springframework.stereotype.Repository
interface JobMissionJpaRepository : JpaRepository<JobMissionEntity, Long> {
    fun findAllByPlayerId(playerId: UUID): List<JobMissionEntity>
}

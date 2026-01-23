package onl.tesseract.srp.repository.hibernate.job.mission

import onl.tesseract.srp.domain.job.mission.JobMission
import onl.tesseract.srp.repository.generic.job.JobMissionRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import java.util.*


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

    override fun save(entity: JobMission): JobMission {
        return jpaRepo.save(entity.toEntity()).toDomain()
    }

    override fun idOf(entity: JobMission): Long {
        return entity.id
    }

    override fun deleteById(id: Long) {
        jpaRepo.deleteById(id)
    }
}

@org.springframework.stereotype.Repository
interface JobMissionJpaRepository : JpaRepository<JobMissionEntity, Long> {
    fun findAllByPlayerId(playerId: UUID): List<JobMissionEntity>
}

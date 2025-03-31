package onl.tesseract.srp.repository.hibernate.job.mission

import jakarta.persistence.QueryHint
import onl.tesseract.lib.repository.Repository
import onl.tesseract.srp.domain.job.EnumJob
import onl.tesseract.srp.domain.job.mission.JobMission
import org.hibernate.jpa.HibernateHints
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.stereotype.Component

interface JobMissionRepository : Repository<JobMission, Long> {
    fun deleteById(id: Long)
    fun findByJob(job: EnumJob): List<JobMission>
    fun findAll(): List<JobMission>
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

    override fun save(entity: JobMission) {
        jpaRepo.save(entity.toEntity())
    }

    override fun idOf(entity: JobMission): Long {
        return entity.id ?: error("JobMission ID is null")
    }

    override fun deleteById(id: Long) {
        jpaRepo.deleteById(id)
    }

    override fun findByJob(job: EnumJob): List<JobMission> {
        return jpaRepo.findByJob(job).map { it.toDomain() }
    }

    override fun findAll(): List<JobMission> {
        return jpaRepo.findAll().map { it.toDomain() }
    }
}

@org.springframework.stereotype.Repository
interface JobMissionJpaRepository : JpaRepository<JobMissionEntity, Long> {
    @QueryHints(QueryHint(name = HibernateHints.HINT_CACHEABLE, value = "true"))
    fun findByJob(job: EnumJob): List<JobMissionEntity>
}

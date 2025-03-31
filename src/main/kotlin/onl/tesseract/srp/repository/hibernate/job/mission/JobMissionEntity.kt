package onl.tesseract.srp.repository.hibernate.job.mission

import jakarta.persistence.*
import onl.tesseract.srp.domain.item.CustomMaterial
import onl.tesseract.srp.domain.job.EnumJob
import onl.tesseract.srp.domain.job.mission.JobMission
import org.hibernate.annotations.CacheConcurrencyStrategy

@Entity
@Table(name = "t_job_missions")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
class JobMissionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val job: EnumJob,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val material: CustomMaterial,

    @Column(nullable = false)
    val quantity: Int,

    @Column(nullable = false)
    val minimalQuality: Int,

    @Column(nullable = false)
    val reward: Int
) {
    fun toDomain(): JobMission {
        return JobMission(
            id = id,
            job = job,
            material = material,
            quantity = quantity,
            minimalQuality = minimalQuality,
            reward = reward
        )
    }
}

fun JobMission.toEntity(): JobMissionEntity {
    return JobMissionEntity(
        id = id,
        job = job,
        material = material,
        quantity = quantity,
        minimalQuality = minimalQuality,
        reward = reward
    )
}

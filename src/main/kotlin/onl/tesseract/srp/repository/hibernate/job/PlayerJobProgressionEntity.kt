package onl.tesseract.srp.repository.hibernate.job

import jakarta.persistence.*
import onl.tesseract.srp.domain.job.EnumJob
import onl.tesseract.srp.domain.job.JobSkill
import onl.tesseract.srp.domain.job.PlayerJobProgression
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.util.*

@Entity
@Table(name = "t_player_job_progression")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
class PlayerJobProgressionEntity(
    @Id
    val playerID: UUID,
    val level: Int,
    val xp: Int,
    val skillPoints: Int,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "t_player_job_reputation",
        joinColumns = [JoinColumn(name = "player_id")]
    )
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "job")
    @Column(name = "reputation")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    val reputationByJob: Map<EnumJob, Double> = EnumJob.entries.associateWith { 1.0 },

    @ElementCollection(fetch = FetchType.EAGER)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    val skills: List<JobSkill>,
) {

    fun toDomain(): PlayerJobProgression {
        return PlayerJobProgression(playerID, level, xp, skillPoints, skills, reputationByJob.toMutableMap())
    }
}

fun PlayerJobProgression.toEntity(): PlayerJobProgressionEntity {
    return PlayerJobProgressionEntity(playerID, level, xp, skillPoints, reputationByJob, skills)
}
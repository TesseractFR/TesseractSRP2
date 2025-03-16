package onl.tesseract.srp.repository.hibernate.job

import jakarta.persistence.*
import onl.tesseract.srp.domain.job.JobSkill
import onl.tesseract.srp.domain.job.PlayerJobProgression
import java.util.*

@Entity
@Table(name = "t_player_job_progression")
class PlayerJobProgressionEntity(
    @Id
    val playerID: UUID,
    val level: Int,
    val xp: Int,
    val skillPoints: Int,
    @ElementCollection(fetch = FetchType.EAGER)
    val skills: List<JobSkill>,
) {

    fun toDomain(): PlayerJobProgression {
        return PlayerJobProgression(playerID, level, xp, skillPoints, skills)
    }
}

fun PlayerJobProgression.toEntity(): PlayerJobProgressionEntity {
    return PlayerJobProgressionEntity(playerID, level, xp, skillPoints, skills)
}
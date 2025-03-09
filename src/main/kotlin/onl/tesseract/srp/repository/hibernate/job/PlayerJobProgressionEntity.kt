package onl.tesseract.srp.repository.hibernate.job

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import onl.tesseract.srp.domain.job.PlayerJobProgression
import java.util.*

@Entity
@Table(name = "t_player_job_progression")
class PlayerJobProgressionEntity(
    @Id
    val playerID: UUID,
    val level: Int,
    val xp: Int,
) {

    fun toDomain(): PlayerJobProgression {
        return PlayerJobProgression(playerID, level, xp)
    }
}

fun PlayerJobProgression.toEntity(): PlayerJobProgressionEntity {
    return PlayerJobProgressionEntity(playerID, level, xp)
}
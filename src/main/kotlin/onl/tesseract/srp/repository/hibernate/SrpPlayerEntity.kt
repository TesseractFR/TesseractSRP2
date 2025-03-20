package onl.tesseract.srp.repository.hibernate

import jakarta.persistence.*
import onl.tesseract.srp.domain.player.PlayerRank
import onl.tesseract.srp.domain.player.SrpPlayer
import java.util.*

@Entity
@Table(name = "t_srp_player")
class SrpPlayerEntity(
    @Id
    var uuid: UUID,
    @Column(name = "player_rank")
    @Enumerated(EnumType.STRING)
    val rank: PlayerRank,
) {

    fun toDomain(): SrpPlayer {
        return SrpPlayer(uuid, rank)
    }
}

fun SrpPlayer.toEntity(): SrpPlayerEntity {
    return SrpPlayerEntity(uniqueId, rank)
}
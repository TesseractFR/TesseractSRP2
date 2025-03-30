package onl.tesseract.srp.repository.hibernate

import jakarta.persistence.*
import onl.tesseract.srp.domain.player.PlayerRank
import onl.tesseract.srp.domain.player.SrpPlayer
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.util.*

@Entity
@Table(name = "t_srp_player")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
class SrpPlayerEntity(
    @Id
    var uuid: UUID,
    @Column(name = "player_rank")
    @Enumerated(EnumType.STRING)
    val rank: PlayerRank,
    val money: Int,
    val titleId: String,
) {

    fun toDomain(): SrpPlayer {
        return SrpPlayer(uuid, rank, money, titleId)
    }
}

fun SrpPlayer.toEntity(): SrpPlayerEntity {
    return SrpPlayerEntity(uniqueId, rank, money, titleID)
}
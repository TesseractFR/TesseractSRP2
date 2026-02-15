package onl.tesseract.srp.repository.hibernate.territory.entity.guild

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import java.util.*

@Entity
@Table(
    name = "t_guild_join_requests",
    indexes = [Index(columnList = "guild_id,player_id", unique = true)]
)
class GuildJoinRequestEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    @Column(name = "player_id", nullable = false)
    val playerId: UUID,
    @Column(nullable = false, length = 256)
    val message: String,
    @Column(name = "requested_date", nullable = false)
    val requestedDate: Instant
) {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guild_id", nullable = false)
    lateinit var guild: GuildEntity
}
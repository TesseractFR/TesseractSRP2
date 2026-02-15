package onl.tesseract.srp.repository.hibernate.territory.entity.guild

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import onl.tesseract.srp.domain.territory.guild.GuildMember
import onl.tesseract.srp.domain.territory.guild.enum.GuildRole
import java.time.Instant
import java.util.*


@Entity
@Table(
    name = "t_guild_members", indexes = [
        Index(columnList = "playerID", unique = true)
    ]
)
class GuildMemberEntity(
    @Id
    val playerID: UUID,
    val role: GuildRole,
    @Column(nullable = false)
    val joinedDate: Instant,
) {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guildID")
    lateinit var guild: GuildEntity

    fun toDomain(): GuildMember = GuildMember(playerID, role, joinedDate)
}
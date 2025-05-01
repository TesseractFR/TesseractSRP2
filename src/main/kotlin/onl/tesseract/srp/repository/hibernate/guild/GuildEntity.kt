package onl.tesseract.srp.repository.hibernate.guild

import jakarta.persistence.*
import onl.tesseract.srp.domain.campement.CampementChunk
import onl.tesseract.srp.domain.guild.Guild
import onl.tesseract.srp.domain.guild.GuildMember
import onl.tesseract.srp.domain.guild.GuildRole
import org.bukkit.Bukkit
import org.bukkit.Location
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.util.*

@Entity
@Table(
    name = "t_guilds", indexes = [
        Index(columnList = "name", unique = true)
    ]
)
class GuildEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,
    val name: String,
    val leaderId: UUID,
    @Embedded
    val spawnLocation: SpawnLocationEntity,
    @OneToMany
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    val chunks: MutableSet<GuildCityChunkEntity>,
    @OneToMany(mappedBy = "guildID")
    val members: MutableList<GuildMemberEntity>,
    @ElementCollection
    val invitations: Set<UUID>,
    @ElementCollection
    val joinRequests: Set<UUID>,
) {

    @Embeddable
    class SpawnLocationEntity(
        val spawnX: Int,
        val spawnY: Int,
        val spawnZ: Int,
    ) {

        fun toLocation(): Location {
            return Location(Bukkit.getWorld("guildWorld"), spawnX.toDouble(), spawnY.toDouble(), spawnZ.toDouble())
        }
    }

    fun toDomain(): Guild {
        return Guild(
            id,
            leaderId,
            name,
            spawnLocation.toLocation(),
            chunks.map { CampementChunk(it.x, it.z) }.toSet(),
            members = members.map { it.toDomain() },
            invitations = invitations,
            joinRequests = joinRequests
        )
    }
}

@Entity
class GuildCityChunkEntity(@Id val x: Int, @Id val z: Int)

@Entity
@Table(
    name = "t_guild_members", indexes = [
        Index(columnList = "playerID", unique = true)
    ]
)
@IdClass(GuildMemberEntity.CompositeId::class)
class GuildMemberEntity(
    @Id
    val playerID: UUID,
    @Id
    val guildID: Int,
    val role: GuildRole,
) {

    data class CompositeId(val playerID: UUID, val guildID: Int)

    fun toDomain(): GuildMember = GuildMember(playerID, role)
}

fun Guild.toEntity(): GuildEntity {
    return GuildEntity(
        id,
        name,
        leaderId,
        GuildEntity.SpawnLocationEntity(spawnLocation.blockX, spawnLocation.blockY, spawnLocation.blockZ),
        chunks.map { GuildCityChunkEntity(it.x, it.z) }.toMutableSet(),
        members = members.map { it.toEntity(id) }.toMutableList(),
        invitations = this.invitations,
        joinRequests = this.joinRequests,
    )
}

fun GuildMember.toEntity(guildId: Int): GuildMemberEntity = GuildMemberEntity(playerID, guildId, role)
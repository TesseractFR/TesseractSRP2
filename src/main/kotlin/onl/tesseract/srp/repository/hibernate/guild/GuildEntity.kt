package onl.tesseract.srp.repository.hibernate.guild

import jakarta.persistence.CascadeType
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import onl.tesseract.srp.domain.campement.CampementChunk
import onl.tesseract.srp.domain.guild.Guild
import onl.tesseract.srp.domain.guild.GuildMember
import onl.tesseract.srp.domain.guild.GuildMemberContainerImpl
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
@Suppress("LongParameterList")
class GuildEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,
    val name: String,
    val leaderId: UUID,
    @Embedded
    val spawnLocation: SpawnLocationEntity,
    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "guild")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    val chunks: MutableSet<GuildCityChunkEntity>,
    @OneToMany(mappedBy = "guildID", cascade = [CascadeType.ALL])
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
            name,
            spawnLocation.toLocation(),
            chunks.map { it.toDomain() }.toSet(),
            GuildMemberContainerImpl(leaderId, members.map { it.toDomain() }, invitations, joinRequests)
        )
    }
}

@Entity
@Table(name = "t_city_chunks")
class GuildCityChunkEntity(
    @Id
    val coordinates: String,
    @ManyToOne(fetch = FetchType.LAZY)
    val guild: GuildEntity? = null,
) {

    constructor(x: Int, z: Int): this("$x,$z")

    fun splitCoordinates(): Pair<Int, Int> {
        val parts = coordinates.split(",")
        return parts[0].toInt() to parts[1].toInt()
    }

    fun toDomain(): CampementChunk {
        val (x, z) = splitCoordinates()
        return CampementChunk(x, z)
    }
}

@Entity
@Table(
    name = "t_guild_members", indexes = [
        Index(columnList = "playerID", unique = true)
    ]
)
class GuildMemberEntity(
    @Id
    val playerID: UUID,
    val guildID: Int,
    val role: GuildRole,
) {

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

package onl.tesseract.srp.repository.hibernate.guild

import jakarta.persistence.*
import onl.tesseract.srp.domain.commun.ChunkCoord
import onl.tesseract.srp.domain.commun.Coordinate
import onl.tesseract.srp.domain.territory.guild.Guild
import onl.tesseract.srp.domain.territory.guild.GuildChunk
import onl.tesseract.srp.domain.territory.guild.GuildMember
import onl.tesseract.srp.domain.territory.guild.GuildMemberContainerImpl
import onl.tesseract.srp.domain.territory.guild.enum.GuildRank
import onl.tesseract.srp.domain.territory.guild.enum.GuildRole
import onl.tesseract.srp.domain.world.SrpWorld
import onl.tesseract.srp.repository.hibernate.territory.entity.guild.GuildChunkEntity
import onl.tesseract.srp.repository.hibernate.territory.entity.guild.toEntity
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.annotations.JdbcTypeCode
import java.sql.Types
import java.util.*
import kotlin.math.floor

private const val CHUNK_SIZE = 16

@Entity
@Table(
    name = "t_guilds", indexes = [
        Index(columnList = "name", unique = true)
    ]
)
@Suppress("LongParameterList")
class GuildEntity(
    @Id
    @Column(name = "id", length = 36, columnDefinition = "VARCHAR(36)")
    @JdbcTypeCode(Types.VARCHAR)
    val id: UUID,
    @Column(unique = true)
    val name: String,
    val leaderId: UUID,
    val money: Int,
    val ledgerId: UUID,
    @Embedded
    val spawnLocation: SpawnLocationEntity,
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "spawnX",    column = Column(name = "visitor_spawn_x")),
        AttributeOverride(name = "spawnY",    column = Column(name = "visitor_spawn_y")),
        AttributeOverride(name = "spawnZ",    column = Column(name = "visitor_spawn_z")),
    )
    val visitorSpawn: SpawnLocationEntity,
    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "guild", orphanRemoval = true, fetch = FetchType.EAGER)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    val chunks: MutableSet<GuildChunkEntity>,
    @OneToMany(mappedBy = "guild", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    val members: MutableList<GuildMemberEntity>,
    @ElementCollection(fetch = FetchType.EAGER)
    val invitations: Set<UUID>,
    @ElementCollection(fetch = FetchType.EAGER)
    val joinRequests: Set<UUID>,
    @Column(nullable = false)
    val level: Int = 1,
    @Column(nullable = false)
    val xp: Int = 0,
    @Enumerated(EnumType.STRING)
    @Column(name = "guild_rank", nullable = false)
    val rank: GuildRank = GuildRank.HAMEAU,
) {

    @Embeddable
    class SpawnLocationEntity(
        val spawnX: Double,
        val spawnY: Double,
        val spawnZ: Double,
    ) {

        fun toCoordinate(): Coordinate {
            return Coordinate(spawnX,spawnY,spawnZ,
                ChunkCoord(
                    floor(spawnX/CHUNK_SIZE).toInt(),
                    floor(spawnZ/CHUNK_SIZE).toInt(),
                    SrpWorld.GuildWorld.bukkitName
                )
            )
        }
    }

    fun toDomain(): Guild {
        val guild = Guild(
            id,
            name,
            spawnLocation.toCoordinate(),
            money,
            ledgerId,
            GuildMemberContainerImpl(leaderId, members.map { it.toDomain() }, invitations, joinRequests),
            visitorSpawnLocation = visitorSpawn.toCoordinate(),
            level = level,
            xp = xp,
            rank = rank
        )
        guild.addChunks(chunks.map { GuildChunk(it.id.toDomain(),guild) }.toSet())
        return guild
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
    val role: GuildRole,
) {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guildID")
    lateinit var guild: GuildEntity

    fun toDomain(): GuildMember = GuildMember(playerID, role)
}

fun Guild.toEntity(): GuildEntity {
    val entity = GuildEntity(
        id = id,
        name = name,
        leaderId = leaderId,
        money = money,
        ledgerId = moneyLedgerID,
        spawnLocation = getSpawnpoint().let { GuildEntity.SpawnLocationEntity(
            it.x, it.y, it.z)},
        visitorSpawn = getVisitorSpawnpoint().let {
            GuildEntity.SpawnLocationEntity(it.x, it.y, it.z)
        },
        chunks = mutableSetOf(),
        members = mutableListOf(),
        invitations = invitations,
        joinRequests = joinRequests,
        level = level,
        xp = xp,
        rank = rank
    )
    entity.chunks.addAll(this.getChunks().map { c -> c.toEntity(entity) })
    entity.members.addAll(this.members.map { m -> GuildMemberEntity(m.playerID, m.role).apply { guild = entity } })
    return entity
}


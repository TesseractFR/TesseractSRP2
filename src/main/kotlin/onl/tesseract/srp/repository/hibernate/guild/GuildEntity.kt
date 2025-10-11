package onl.tesseract.srp.repository.hibernate.guild

import jakarta.persistence.*
import onl.tesseract.srp.domain.guild.*
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
    val visitorSpawn: SpawnLocationEntity? = null,
    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "guild", orphanRemoval = true, fetch = FetchType.EAGER)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    val chunks: MutableSet<GuildCityChunkEntity>,
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
            money,
            ledgerId,
            chunks.map { it.toDomain() }.toSet(),
            GuildMemberContainerImpl(leaderId, members.map { it.toDomain() }, invitations, joinRequests),
            visitorSpawnLocation = visitorSpawn?.toLocation(),
            level = level,
            xp = xp,
            rank = rank
        )
    }
}

@Entity
@Table(name = "t_guild_chunks")
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

    fun toDomain(): GuildChunk {
        val (x, z) = splitCoordinates()
        return GuildChunk (x, z)
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
        spawnLocation = GuildEntity.SpawnLocationEntity(
            spawnLocation.blockX, spawnLocation.blockY, spawnLocation.blockZ),
        visitorSpawn = visitorSpawnLocation?.let {
            GuildEntity.SpawnLocationEntity(it.blockX, it.blockY, it.blockZ)
        },
        chunks = mutableSetOf(),
        members = mutableListOf(),
        invitations = invitations,
        joinRequests = joinRequests,
        level = level,
        xp = xp,
        rank = rank
    )
    entity.chunks.addAll(this.chunks.map { c -> GuildCityChunkEntity("${c.x},${c.z}", entity) })
    entity.members.addAll(this.members.map { m -> GuildMemberEntity(m.playerID, m.role).apply { guild = entity } })
    return entity
}


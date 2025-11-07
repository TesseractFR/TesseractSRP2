package onl.tesseract.srp.repository.hibernate.guild

import jakarta.persistence.*
import onl.tesseract.srp.domain.guild.*
import onl.tesseract.srp.domain.territory.guild.Guild
import onl.tesseract.srp.domain.territory.guild.GuildMember
import onl.tesseract.srp.domain.territory.guild.GuildMemberContainerImpl
import onl.tesseract.srp.domain.territory.guild.enum.GuildRole
import onl.tesseract.srp.repository.hibernate.territory.entity.guild.GuildChunkEntity
import onl.tesseract.srp.repository.hibernate.territory.entity.guild.toEntity
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
    val visitorSpawn: SpawnLocationEntity? = null,
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
        val spawnX: Int,
        val spawnY: Int,
        val spawnZ: Int,
    ) {

        fun toLocation(): Location {
            return Location(Bukkit.getWorld("guildWorld"), spawnX.toDouble(), spawnY.toDouble(), spawnZ.toDouble())
        }
    }

    fun toDomain(): Guild {
        val guild = Guild(
            id,
            name,
            spawnLocation.toLocation(),
            money,
            ledgerId,
            GuildMemberContainerImpl(leaderId, members.map { it.toDomain() }, invitations, joinRequests),
            visitorSpawnLocation = visitorSpawn?.toLocation(),
            level = level,
            xp = xp,
            rank = rank
        )
        guild.addChunks(chunks.map { it.toDomain() }.toSet())
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
    entity.chunks.addAll(this.getChunks().map { c -> c.toEntity(entity) })
    entity.members.addAll(this.members.map { m -> GuildMemberEntity(m.playerID, m.role).apply { guild = entity } })
    return entity
}


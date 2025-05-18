package onl.tesseract.srp.domain.guild

import onl.tesseract.srp.domain.campement.CampementChunk
import org.bukkit.Location
import java.util.*

class Guild(
    val id: Int,
    val name: String,
    val spawnLocation: Location,
    money: Int = 0,
    val moneyLedgerID: UUID = UUID.randomUUID(),
    chunks: Set<CampementChunk> = setOf(),
    memberContainer: GuildMemberContainerImpl,
) : GuildMemberContainer by memberContainer {
    private val _chunks: MutableSet<CampementChunk> = chunks.toMutableSet()
    val chunks: Set<CampementChunk>
        get() = _chunks

    var money: Int = money
        private set

    constructor(id: Int, leaderId: UUID, name: String, spawnLocation: Location)
            : this(id, name, spawnLocation, memberContainer = GuildMemberContainerImpl(leaderId))

    /**
     * @throws IllegalStateException If the guild already has chunks
     */
    fun claimInitialChunks() {
        check(chunks.isEmpty())

        val spawnChunk = spawnLocation.chunk
        for (x in -1..1) {
            for (z in -1..1) {
                _chunks.add(CampementChunk(spawnChunk.x + x, spawnChunk.z + z))
            }
        }
    }

    fun addMoney(amount: Int) {
        money += amount
    }

    /**
     * @throws IllegalArgumentException If the player is not a member of the guild
     */
    fun getMemberRole(member: UUID): GuildRole {
        return members.find { it.playerID == member }?.role
            ?: throw IllegalArgumentException("Player $member is not a member of guild $id")
    }
}

interface GuildMemberContainer {
    val leaderId: UUID
    val members: List<GuildMember>
    val invitations: Set<UUID>
    val joinRequests: Set<UUID>

    fun invitePlayer(playerID: UUID)
    fun askToJoin(playerID: UUID)
    fun join(playerID: UUID)
}

class GuildMemberContainerImpl(
    override val leaderId: UUID,
    members: List<GuildMember> = listOf(),
    invitations: Set<UUID> = setOf(),
    joinRequests: Set<UUID> = setOf(),
) : GuildMemberContainer {
    private val _members = members.toMutableList()
    override val members: List<GuildMember> get() = _members

    private val _invitations = invitations.toMutableSet()
    override val invitations: Set<UUID> get() = _invitations

    private val _joinRequests = joinRequests.toMutableSet()
    override val joinRequests: Set<UUID> get() = _joinRequests

    init {
        if (members.none { it.playerID == leaderId })
            _members.add(GuildMember(leaderId, GuildRole.Leader))
    }

    override fun invitePlayer(playerID: UUID) {
        require(members.none { it.playerID == playerID })
        _invitations.add(playerID)
    }

    override fun askToJoin(playerID: UUID) {
        require(members.none { it.playerID == playerID })
        _joinRequests.add(playerID)
    }

    override fun join(playerID: UUID) {
        require(members.none { it.playerID == playerID })
        _invitations.remove(playerID)
        _joinRequests.remove(playerID)
        _members.add(GuildMember(playerID, GuildRole.Citoyen))
    }
}

class GuildMember(
    val playerID: UUID,
    val role: GuildRole,
)

enum class GuildRole {
    Citoyen,
    Batisseur,
    Adjoint,
    Leader,
    ;

    fun canWithdrawMoney(): Boolean = this >= Adjoint
}

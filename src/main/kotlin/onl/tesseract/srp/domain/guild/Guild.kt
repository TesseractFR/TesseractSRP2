package onl.tesseract.srp.domain.guild

import org.bukkit.Location
import java.util.*

class Guild(
    var id: Int,
    val name: String,
    var spawnLocation: Location,
    money: Int = 0,
    val moneyLedgerID: UUID = UUID.randomUUID(),
    chunks: Set<GuildChunk> = setOf(),
    memberContainer: GuildMemberContainerImpl,
    var visitorSpawnLocation: Location? = null,
    var level: Int = 1,
    var xp: Int = 0,
    var rank: GuildRank = GuildRank.HAMEAU
) : GuildMemberContainer by memberContainer {
    private val _chunks: MutableSet<GuildChunk> = chunks.toMutableSet()
    val chunks: Set<GuildChunk>
        get() = _chunks

    var money: Int = money

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
                _chunks.add(GuildChunk(spawnChunk.x + x, spawnChunk.z + z))
            }
        }
    }

    /**
     * Sets the spawn point of the guild.
     * The new location must be within one of the guild's chunks.
     * @return true if the spawn point was set, false otherwise
     */
    fun setSpawnpoint(newLocation: Location): Boolean {
        if (!chunks.contains(GuildChunk(newLocation))) return false
        spawnLocation = newLocation
        return true
    }

    fun setVisitorSpawnpoint(newLocation: Location): Boolean {
        if (!chunks.contains(GuildChunk(newLocation))) return false
        visitorSpawnLocation = newLocation
        return true
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

    fun addChunk(chunk: GuildChunk): Boolean = _chunks.add(chunk)
    fun removeChunk(chunk: GuildChunk): Boolean = _chunks.remove(chunk)

    fun addXp(amount: Int) { xp += amount.coerceAtLeast(0) }
}

interface GuildMemberContainer {
    var leaderId: UUID
    val members: List<GuildMember>
    val invitations: Set<UUID>
    val joinRequests: Set<UUID>

    fun invitePlayer(playerID: UUID)
    fun askToJoin(playerID: UUID)
    fun join(playerID: UUID)
    fun removeInvitation(playerID: UUID): Boolean
    fun removeMember(playerID: UUID): Boolean
}

class GuildMemberContainerImpl(
    override var leaderId: UUID,
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

    override fun removeInvitation(playerID: UUID): Boolean = _invitations.remove(playerID)
    override fun removeMember(playerID: UUID): Boolean {
        if (playerID == leaderId) return false
        _invitations.remove(playerID)
        _joinRequests.remove(playerID)
        return _members.removeIf { it.playerID == playerID }
    }
}

class GuildMember(
    val playerID: UUID,
    var role: GuildRole,
)

data class GuildChunk(val x: Int, val z: Int) {
    constructor(location: Location): this(location.chunk.x, location.chunk.z)
    override fun toString(): String = "($x, $z)"
}

enum class GuildRole {
    Citoyen,
    Batisseur,
    Adjoint,
    Leader,
    ;

    fun setRole(newRole: GuildRole): Boolean =
        this != Leader && newRole != Leader

    fun canWithdrawMoney(): Boolean = this >= Adjoint
}

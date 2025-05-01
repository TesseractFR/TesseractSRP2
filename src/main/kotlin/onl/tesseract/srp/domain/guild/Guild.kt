package onl.tesseract.srp.domain.guild

import onl.tesseract.srp.domain.campement.CampementChunk
import org.bukkit.Location
import java.util.*

class Guild(
    val id: Int,
    val leaderId: UUID,
    val name: String,
    val spawnLocation: Location,
    chunks: Set<CampementChunk> = setOf(),
    members: List<GuildMember> = listOf(),
    invitations: Set<UUID> = setOf(),
    joinRequests: Set<UUID> = setOf(),
) {

    private val _chunks: MutableSet<CampementChunk> = chunks.toMutableSet()
    val chunks: Set<CampementChunk>
        get() = _chunks

    private val _members: MutableList<GuildMember> = members.toMutableList()
    val members: List<GuildMember>
        get() = _members

    private val _invitations: MutableSet<UUID> = invitations.toMutableSet()
    val invitations: Set<UUID>
        get() = _invitations

    private val _joinRequests: MutableSet<UUID> = joinRequests.toMutableSet()
    val joinRequests: Set<UUID>
        get() = _joinRequests

    init {
        if (members.none { it.playerID == leaderId })
            _members.add(GuildMember(leaderId, GuildRole.Leader))
    }

    /**
     * @throws IllegalStateException If the guild already has chunks
     */
    fun claimInitialChunks() {
        check(chunks.isEmpty())

        val spawnChunk = spawnLocation.chunk
        for (x in -1 .. 1) {
            for (z in -1 .. 1) {
                _chunks.add(CampementChunk(spawnChunk.x + x, spawnChunk.z + z))
            }
        }
    }

    fun invitePlayer(playerID: UUID) {
        require(members.none { it.playerID == playerID })
        _invitations.add(playerID)
    }

    fun askToJoin(playerID: UUID) {
        require(members.none { it.playerID == playerID })
        _joinRequests.add(playerID)
    }

    fun join(playerID: UUID) {
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

enum class GuildRole { Leader, Adjoint, Batisseur, Citoyen }
package onl.tesseract.srp.domain.territory.guild

import onl.tesseract.srp.domain.territory.guild.enum.GuildRole
import java.time.Instant
import java.util.UUID

class GuildMemberContainerImpl(
    override var leaderId: UUID,
    members: List<GuildMember> = listOf(),
    invitations: Set<UUID> = setOf(),
    joinRequests: List<GuildJoinRequest> = listOf(),
) : GuildMemberContainer {
    private val _members = members.toMutableList()
    override val members: List<GuildMember> get() = _members

    private val _invitations = invitations.toMutableSet()
    override val invitations: Set<UUID> get() = _invitations

    private val _joinRequests = joinRequests.toMutableList()
    override val joinRequests: List<GuildJoinRequest> get() = _joinRequests

    init {
        if (members.none { it.playerID == leaderId })
            _members.add(GuildMember(leaderId, GuildRole.Leader, Instant.now()))
    }

    override fun invitePlayer(playerID: UUID) {
        require(members.none { it.playerID == playerID })
        _invitations.add(playerID)
    }

    override fun askToJoin(playerID: UUID, message: String) {
        require(members.none { it.playerID == playerID })
        if (_joinRequests.any { it.playerID == playerID }) return
        _joinRequests.add(GuildJoinRequest(UUID.randomUUID(), playerID, message, Instant.now()))
    }

    override fun join(playerID: UUID) {
        require(members.none { it.playerID == playerID })
        _invitations.remove(playerID)
        _joinRequests.removeIf { it.playerID == playerID }
        _members.add(GuildMember(playerID, GuildRole.Citoyen, Instant.now()))
    }

    override fun removeInvitation(playerID: UUID): Boolean = _invitations.remove(playerID)
    override fun removeJoinRequest(playerID: UUID): Boolean =
        _joinRequests.removeIf { it.playerID == playerID }

    override fun removeMember(playerID: UUID): Boolean {
        if (playerID == leaderId) return false
        _invitations.remove(playerID)
        _joinRequests.removeIf { it.playerID == playerID }
        return _members.removeIf { it.playerID == playerID }
    }
}


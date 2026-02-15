package onl.tesseract.srp.domain.territory.guild

import java.util.UUID

interface GuildMemberContainer {
    var leaderId: UUID
    val members: List<GuildMember>
    val invitations: Set<UUID>
    val joinRequests: List<GuildJoinRequest>

    fun invitePlayer(playerID: UUID)
    fun askToJoin(playerID: UUID, message: String)
    fun join(playerID: UUID)
    fun removeInvitation(playerID: UUID): Boolean
    fun removeJoinRequest(playerID: UUID): Boolean
    fun removeMember(playerID: UUID): Boolean
}

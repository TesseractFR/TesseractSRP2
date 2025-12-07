package onl.tesseract.srp.domain.territory.guild

import java.util.UUID

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

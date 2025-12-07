package onl.tesseract.srp.domain.territory.guild.event

import java.util.*

data class GuildInvitationEvent(
    val guild: String,
    val sender: UUID,
    val target: UUID
)

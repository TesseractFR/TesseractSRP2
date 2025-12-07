package onl.tesseract.srp.domain.territory.guild

import onl.tesseract.srp.domain.territory.guild.enum.GuildRole
import java.util.UUID

class GuildMember(
    val playerID: UUID,
    var role: GuildRole,
)

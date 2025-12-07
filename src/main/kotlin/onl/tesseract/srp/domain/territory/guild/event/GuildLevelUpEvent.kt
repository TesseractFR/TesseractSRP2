package onl.tesseract.srp.domain.territory.guild.event

import onl.tesseract.srp.domain.territory.guild.Guild

data class GuildLevelUpEvent(
    val guild: Guild,
    val level: Int
)

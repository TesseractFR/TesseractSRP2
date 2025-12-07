package onl.tesseract.srp.domain.territory.guild.event

import onl.tesseract.srp.domain.territory.event.TerritoryUnclaimEvent
import onl.tesseract.srp.domain.territory.guild.GuildChunk
import java.util.*

class GuildChunkUnclaimEvent(playerId: UUID) : TerritoryUnclaimEvent<GuildChunk>(playerId)
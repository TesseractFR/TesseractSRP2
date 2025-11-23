package onl.tesseract.srp.domain.territory.guild.event

import onl.tesseract.srp.domain.territory.event.TerritoryClaimEvent
import onl.tesseract.srp.domain.territory.guild.GuildChunk
import java.util.*

class GuildChunkClaimEvent(playerId: UUID) : TerritoryClaimEvent<GuildChunk>(playerId)

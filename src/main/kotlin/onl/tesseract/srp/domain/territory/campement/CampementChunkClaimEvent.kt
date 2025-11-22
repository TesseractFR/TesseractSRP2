package onl.tesseract.srp.domain.territory.campement

import onl.tesseract.srp.domain.territory.TerritoryClaimEvent
import java.util.*

class CampementChunkClaimEvent(
    playerId: UUID
) : TerritoryClaimEvent<CampementChunk>(playerId)

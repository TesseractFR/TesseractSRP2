package onl.tesseract.srp.domain.territory.campement

import onl.tesseract.srp.domain.territory.TerritoryUnclaimEvent
import java.util.*

class CampementChunkUnclaimEvent(
    playerId: UUID
) : TerritoryUnclaimEvent<CampementChunk>(playerId) {
}

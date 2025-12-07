package onl.tesseract.srp.domain.territory.event

import onl.tesseract.srp.domain.territory.TerritoryChunk
import java.util.*

abstract class TerritoryClaimEvent<TC : TerritoryChunk>(val playerId: UUID)

abstract class TerritoryUnclaimEvent<TC : TerritoryChunk>(val playerId: UUID)

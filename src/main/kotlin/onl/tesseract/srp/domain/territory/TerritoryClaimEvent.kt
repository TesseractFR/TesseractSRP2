package onl.tesseract.srp.domain.territory

import org.bukkit.event.Event
import java.util.*

abstract class TerritoryClaimEvent<TC : TerritoryChunk>(val playerId: UUID) : Event(){
}

abstract class TerritoryUnclaimEvent<TC : TerritoryChunk>(val playerId: UUID) : Event(){
}
package onl.tesseract.srp.domain.territory.campement

import onl.tesseract.srp.domain.territory.TerritoryUnclaimEvent
import org.bukkit.event.HandlerList
import java.util.*

class CampementChunkUnclaimEvent(
    playerId: UUID
) : TerritoryUnclaimEvent<CampementChunk>(playerId) {

    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}

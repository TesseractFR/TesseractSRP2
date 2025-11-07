package onl.tesseract.srp.domain.territory.campement

import onl.tesseract.srp.domain.territory.TerritoryClaimEvent
import org.bukkit.event.HandlerList
import java.util.*

class CampementChunkClaimEvent(
    playerId: UUID
) : TerritoryClaimEvent<CampementChunk>(playerId) {

    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}

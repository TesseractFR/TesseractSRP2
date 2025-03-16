package onl.tesseract.srp.controller.event.campement

import onl.tesseract.srp.domain.campement.CampementChunk
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.*

class CampementChunkClaimEvent(
    val playerId: UUID,
    val chunk: CampementChunk
) : Event() {

    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}

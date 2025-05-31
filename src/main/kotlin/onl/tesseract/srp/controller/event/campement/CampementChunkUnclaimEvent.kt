package onl.tesseract.srp.controller.event.campement

import onl.tesseract.srp.domain.Claim
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.*

class CampementChunkUnclaimEvent(
    val playerId: UUID,
    val chunk: Claim
) : Event() {

    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}

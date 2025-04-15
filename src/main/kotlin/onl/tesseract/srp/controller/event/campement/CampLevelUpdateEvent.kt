package onl.tesseract.srp.controller.event.campement

import onl.tesseract.srp.domain.player.PlayerRank
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.*

class CampLevelUpdateEvent(
    val playerId: UUID,
    val newRank: PlayerRank
) : Event() {

    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}

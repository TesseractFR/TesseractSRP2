package onl.tesseract.srp.controller.event.player

import onl.tesseract.srp.domain.player.PlayerRank
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.*

class PlayerRankUpEvent(
    val playerId: UUID,
    val newRank: PlayerRank
) : Event() {

    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}

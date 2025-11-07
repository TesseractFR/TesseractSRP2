package onl.tesseract.srp.domain.territory.guild

import onl.tesseract.srp.domain.territory.TerritoryUnclaimEvent
import org.bukkit.event.HandlerList
import java.util.*

class GuildChunkUnclaimEvent(playerId: UUID) : TerritoryUnclaimEvent<GuildChunk>(playerId) {
    override fun getHandlers(): HandlerList = handlerList
    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}

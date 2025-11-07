package onl.tesseract.srp.domain.territory.guild

import onl.tesseract.srp.domain.territory.TerritoryClaimEvent
import org.bukkit.event.HandlerList
import java.util.*

class GuildChunkClaimEvent(playerId: UUID) : TerritoryClaimEvent<GuildChunk>(playerId) {
    override fun getHandlers(): HandlerList = handlerList
    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}

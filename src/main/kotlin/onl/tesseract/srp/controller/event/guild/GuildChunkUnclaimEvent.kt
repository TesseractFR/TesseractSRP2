package onl.tesseract.srp.controller.event.guild

import onl.tesseract.srp.domain.guild.GuildChunk
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.*

class GuildChunkUnclaimEvent(val playerId: UUID, val chunk: GuildChunk) : Event() {
    override fun getHandlers(): HandlerList = handlerList
    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
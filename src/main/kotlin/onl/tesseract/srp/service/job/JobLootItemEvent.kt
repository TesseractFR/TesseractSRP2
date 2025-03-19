package onl.tesseract.srp.service.job

import onl.tesseract.srp.domain.item.CustomItem
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.*

data class JobLootItemEvent(
    val playerID: UUID,
    val item: CustomItem,
    val xp: Int,
) : Event() {

    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
package onl.tesseract.srp.service.job

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.UUID

/**
 * Called when a player gains a job level
 */
data class PlayerLevelUpEvent(
    val playerID: UUID,
    /**
     * New job level
     */
    val level: Int,
    /**
     * Amount of levels passed simultaneously
     */
    val passedLevel: Int
) : Event() {
    override fun getHandlers(): HandlerList = handlerList

    override fun toString() = "Player $playerID has passed level $level (+$passedLevel)"

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
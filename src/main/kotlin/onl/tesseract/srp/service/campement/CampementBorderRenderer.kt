package onl.tesseract.srp.service.campement

import onl.tesseract.srp.util.TerritoryBordersManager
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import org.springframework.stereotype.Component

@Component
open class CampementBorderRenderer {

    private val activeBorders = mutableMapOf<Player, BukkitTask>()

    /**
     * Displays the camp borders using End Rod particles.
     * @param player The player who will see the borders.
     * @param chunks List of chunk coordinates [x, z].
     */
    fun showBorders(player: Player, chunks: List<List<Int>>) {
        clearBorders(player)
        activeBorders[player] = TerritoryBordersManager.startBorderTask(
            player = player,
            chunksProvider = { chunks },
            x = { it[0] },
            z = { it[1] }
        )
    }

    /**
     * Clears the previously displayed borders for a player.
     * @param player The player whose borders should be cleared.
     */
    fun clearBorders(player: Player) {
        activeBorders.remove(player)?.cancel()
    }

    /**
     * Checks if the borders are currently being displayed for a player.
     * @param player The player to check.
     * @return True if borders are active, False otherwise.
     */
    fun isShowingBorders(player: Player): Boolean {
        return activeBorders.containsKey(player)
    }

}



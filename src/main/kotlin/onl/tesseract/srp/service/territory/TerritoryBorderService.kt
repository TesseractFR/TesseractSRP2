package onl.tesseract.srp.service.territory

import onl.tesseract.srp.domain.territory.Territory
import onl.tesseract.srp.domain.territory.TerritoryChunk
import onl.tesseract.srp.infrastructure.scheduler.territory.TerritoryBorderTaskScheduler
import java.util.*


abstract class TerritoryBorderService<TC : TerritoryChunk, T : Territory<TC>>{

    abstract protected val scheduler: TerritoryBorderTaskScheduler
    abstract protected val territoryService: TerritoryService<TC,T>

    /**
     * Displays the camp borders using End Rod particles.
     * @param player The player who will see the borders.
     */
    fun showBorders(player: UUID) {
        clearBorders(player)
        scheduler.schedule(player, territoryService.getByPlayer(player)
                ?.getChunks()
                ?.map { it.chunkCoord } ?: return
        )
    }

    /**
     * Clears the previously displayed borders for a player.
     * @param player The player whose borders should be cleared.
     */
    fun clearBorders(player: UUID) {
        scheduler.cancel(player)
    }

    /**
     * Checks if the borders are currently being displayed for a player.
     * @param player The player to check.
     * @return True if borders are active, False otherwise.
     */
    fun isShowingBorders(player: UUID): Boolean {
        return scheduler.isActive(player)
    }

}
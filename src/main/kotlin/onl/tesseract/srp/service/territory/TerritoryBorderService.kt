package onl.tesseract.srp.service.territory

import onl.tesseract.srp.domain.territory.Territory
import onl.tesseract.srp.domain.territory.TerritoryChunk
import onl.tesseract.srp.domain.territory.enum.result.BorderResult
import onl.tesseract.srp.infrastructure.scheduler.territory.TerritoryBorderTaskScheduler
import java.util.*


abstract class TerritoryBorderService<TC : TerritoryChunk, T : Territory<TC>>{

    protected abstract val scheduler: TerritoryBorderTaskScheduler
    protected abstract val territoryService: TerritoryService<TC,T>

    /**
     * Toggles the display of camp borders for a player.
     * If borders are currently shown, they will be cleared; if not, they will be displayed.
     * @param playerId The UUID of the player.
     * @param currentWorld The name of the world the player is currently in.
     */
    fun toggleBorders(playerId: UUID, currentWorld: String): BorderResult {
        if (!territoryService.isCorrectWorld(currentWorld)) { return BorderResult.INVALID_WORLD }
        territoryService.getByPlayer(playerId)
            ?: return BorderResult.TERRITORY_NOT_FOUND
        return if (isShowingBorders(playerId)) {
            clearBorders(playerId)
            BorderResult.CLEAR_BORDERS
        } else {
            showBorders(playerId)
            BorderResult.SHOW_BORDERS
        }
    }

    /**
     * Displays the camp borders using End Rod particles.
     * @param player The player who will see the borders.
     */
    private fun showBorders(player: UUID) {
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
    private fun isShowingBorders(player: UUID): Boolean {
        return scheduler.isActive(player)
    }

    /**
     * Refreshes the borders for a player if they are currently being displayed.
     * @param playerId The UUID of the player.
     */
    fun refreshBordersIfShowing(playerId: UUID) {
        if (!isShowingBorders(playerId)) return
        showBorders(playerId)
    }

}
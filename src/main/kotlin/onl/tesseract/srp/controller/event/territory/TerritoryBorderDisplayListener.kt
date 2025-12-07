package onl.tesseract.srp.controller.event.territory

import onl.tesseract.srp.domain.territory.Territory
import onl.tesseract.srp.domain.territory.TerritoryChunk
import onl.tesseract.srp.service.territory.TerritoryBorderService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

abstract class TerritoryBorderDisplayListener<TC : TerritoryChunk, T : Territory<TC>>(
    private val borderService: TerritoryBorderService<TC, T>
) : Listener {

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) =
        borderService.clearBorders(event.player.uniqueId)

    @EventHandler
    fun onKick(event: PlayerKickEvent) =
        borderService.clearBorders(event.player.uniqueId)

    protected fun updateBorders(playerId: UUID) {
        borderService.refreshBordersIfShowing(playerId)
    }
}

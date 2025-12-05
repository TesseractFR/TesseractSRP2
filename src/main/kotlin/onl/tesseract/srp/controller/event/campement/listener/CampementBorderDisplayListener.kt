package onl.tesseract.srp.controller.event.campement.listener

import onl.tesseract.srp.domain.territory.campement.CampementChunkClaimEvent
import onl.tesseract.srp.domain.territory.campement.CampementChunkUnclaimEvent
import onl.tesseract.srp.service.territory.campement.CampementBorderRenderer
import onl.tesseract.srp.service.territory.campement.CampementService
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.springframework.context.event.EventListener
import java.util.*
import org.springframework.stereotype.Component

@Component
class CampementBorderDisplayListener(
    private val borderRenderer: CampementBorderRenderer,
    private val campementService: CampementService
) : Listener {

    @EventListener
    fun onChunkClaim(event: CampementChunkClaimEvent) {
        updateBorders(event.playerId)
    }

    @EventListener
    fun onChunkUnclaim(event: CampementChunkUnclaimEvent) {
        updateBorders(event.playerId)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) = borderRenderer.clearBorders(event.player.uniqueId)

    @EventHandler
    fun onKick(event: PlayerKickEvent) = borderRenderer.clearBorders(event.player.uniqueId)

    private fun updateBorders(playerId: UUID) {
        val player = Bukkit.getPlayer(playerId)
        val campement = campementService.getCampementByOwner(playerId)
        if (player != null && campement != null && borderRenderer.isShowingBorders(player.uniqueId)) {
            borderRenderer.showBorders(
                player.uniqueId
            )
        }
    }

}


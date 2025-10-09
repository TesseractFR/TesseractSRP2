package onl.tesseract.srp.controller.event.campement.listener

import onl.tesseract.srp.controller.event.campement.CampementChunkClaimEvent
import onl.tesseract.srp.controller.event.campement.CampementChunkUnclaimEvent
import onl.tesseract.srp.service.campement.CampementBorderRenderer
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import org.springframework.stereotype.Component

@Component
class CampementBorderDisplayListener(
    private val borderRenderer: CampementBorderRenderer,
    private val campementService: CampementService
) : Listener {

    @EventHandler
    fun onChunkClaim(event: CampementChunkClaimEvent) {
        updateBorders(event.playerId)
    }

    @EventHandler
    fun onChunkUnclaim(event: CampementChunkUnclaimEvent) {
        updateBorders(event.playerId)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) = borderRenderer.clearBorders(event.player)

    @EventHandler
    fun onKick(event: PlayerKickEvent) = borderRenderer.clearBorders(event.player)

    private fun updateBorders(playerId: UUID) {
        val player = Bukkit.getPlayer(playerId) ?: return
        val campement = campementService.getCampementByOwner(playerId) ?: return
        if (!borderRenderer.isShowingBorders(player)) return
        val chunks = campement.chunks.map { listOf(it.x, it.z) }
        borderRenderer.showBorders(player, chunks)
    }
}


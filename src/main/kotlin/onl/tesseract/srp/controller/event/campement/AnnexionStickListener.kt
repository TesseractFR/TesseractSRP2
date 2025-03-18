package onl.tesseract.srp.controller.event.campement

import onl.tesseract.srp.service.campement.CampementService
import onl.tesseract.srp.domain.campement.AnnexationStick
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.springframework.stereotype.Component

@Component
class AnnexationStickListener(private val campementService: CampementService) : Listener {

    @EventHandler
    fun onPlayerUseStick(event: PlayerInteractEvent) {
        val player = event.player
        val item = player.inventory.itemInMainHand

        if (!AnnexationStick.isAnnexationStick(item)) return

        val chunk = "${player.location.chunk.x},${player.location.chunk.z}"
        val claim = when (event.action) {
            org.bukkit.event.block.Action.RIGHT_CLICK_AIR, org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK -> true
            org.bukkit.event.block.Action.LEFT_CLICK_AIR, org.bukkit.event.block.Action.LEFT_CLICK_BLOCK -> false
            else -> return
        }

        player.sendMessage(campementService.handleClaimUnclaim(player.uniqueId, chunk, claim))
        event.isCancelled = true
    }
}

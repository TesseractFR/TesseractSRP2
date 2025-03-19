package onl.tesseract.srp.controller.event.campement

import onl.tesseract.srp.service.campement.CampementService
import onl.tesseract.srp.domain.campement.AnnexationStick
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.springframework.stereotype.Component

@Component
class AnnexationStickListener(private val campementService: CampementService) : Listener {

    @EventHandler
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        if (AnnexationStick.isAnnexationStick(event.itemDrop.itemStack)) {
            event.isCancelled = true
            event.player.sendMessage("§cTu ne peux pas jeter ton Bâton d'Annexion !")
        }
    }  // PROBLEME : quand je jette le bâton, ça me simule une action de clic gauche, donc comme si je voulais unclaim !!!

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val item = event.currentItem ?: return

        if (AnnexationStick.isAnnexationStick(item)) {
            event.isCancelled = true
            if (event.isShiftClick) {
                AnnexationStick.removeFromInventory(player)
                player.sendMessage("§eTu as retiré ton Bâton d'Annexion.")
            }
        }
    }

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

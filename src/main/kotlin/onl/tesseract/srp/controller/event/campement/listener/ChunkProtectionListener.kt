package onl.tesseract.srp.controller.event.campement.listener

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.service.campement.CampementService
import onl.tesseract.srp.service.campement.InteractionAllowResult
import onl.tesseract.srp.util.CampementChatError
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.EquipmentSlot
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
class ChunkProtectionListener(private val campementService: CampementService) : Listener {

    private fun getProtectionMessage(player: Player, chunk: Chunk, cancel: () -> Unit) {
        if (campementService.canInteractInChunk(player.uniqueId, chunk) != InteractionAllowResult.Deny) return
        cancel()
        val camp = campementService.getCampementByChunk(chunk.x, chunk.z)
        val msg = if (camp == null) {
            CampementChatError + "Tu ne peux pas interagir dans la nature."
        } else {
            val ownerName = Bukkit.getOfflinePlayer(camp.ownerID).name ?: "Inconnu"
            CampementChatError + "Tu ne peux pas interagir ici ! Ce terrain appartient à " +
                    Component.text(ownerName, NamedTextColor.GOLD) + "."
        }
        player.sendMessage(msg)
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND
            || event.action != Action.RIGHT_CLICK_BLOCK
            || event.clickedBlock == null) {
            return
        }
        getProtectionMessage(event.player, event.clickedBlock!!.chunk) { event.isCancelled = true }
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        getProtectionMessage(event.player, event.block.chunk) { event.isCancelled = true }
    }
}

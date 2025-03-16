package onl.tesseract.srp.controller.event.campement.listener

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.service.campement.CampementService
import onl.tesseract.srp.util.CampementChatError
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
class ChunkProtectionListener(private val campementService: CampementService) : Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        val player = event.player
        val location = event.clickedBlock?.location ?: event.player.location
        val chunk = location.chunk

        if (!campementService.canInteractInChunk(player.uniqueId, chunk.x, chunk.z)) {
            val campement = campementService.getCampementByChunk(chunk.x, chunk.z)
            val ownerName = campement?.ownerID?.let { Bukkit.getOfflinePlayer(it).name } ?: "Inconnu"

            event.isCancelled = true
            player.sendMessage(
                CampementChatError + "Tu ne peux pas interagir ici ! Ce terrain appartient à "
                        + Component.text(ownerName, NamedTextColor.GOLD) + "."
            )
        }
    }
}

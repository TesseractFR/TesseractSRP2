package onl.tesseract.srp.controller.event.campement

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.lib.util.ChatFormats
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
class ChunkProtectionListener(private val campementService: CampementService) : Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val block = event.clickedBlock ?: return
        val chunkKey = "${block.chunk.x},${block.chunk.z}"

        if (!campementService.canInteractInChunk(player.uniqueId, chunkKey)) {
            val campement = campementService.getCampementByChunk(chunkKey)
            val ownerName = campement?.ownerID?.let { Bukkit.getOfflinePlayer(it).name } ?: "Inconnu"

            event.isCancelled = true
            player.sendMessage(
                ChatFormats.CHAT_ERROR.append(
                    Component.text("Tu ne peux pas interagir ici ! Ce terrain appartient à ")
                ).append(
                    Component.text(ownerName, NamedTextColor.GOLD)
                ).append(
                    Component.text(".")
                )
            )
        }
    }
}

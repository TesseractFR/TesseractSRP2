package onl.tesseract.srp.controller.event.campement

import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.*
import org.springframework.stereotype.Component

@Component
class ChunkProtectionListener(private val campementService: CampementService) : Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val block = event.clickedBlock ?: return
        val chunkKey = "${block.chunk.x},${block.chunk.z}"

        if (!campementService.canInteractInChunk(player.uniqueId, chunkKey)) {
            event.isCancelled = true
            player.sendMessage("§cTu ne peux pas interagir ici ! Ce terrain appartient à un joueur.")
        }
    }
}

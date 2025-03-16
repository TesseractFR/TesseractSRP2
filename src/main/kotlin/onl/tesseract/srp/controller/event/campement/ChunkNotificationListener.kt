package onl.tesseract.srp.controller.event.campement

import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.springframework.stereotype.Component
import java.util.*

/**
 * Displays the camp name or "Nature" depending on the chunk the player moves into.
 */
@Component
class ChunkNotificationListener(private val campementService: CampementService) : Listener {

    private val lastCampementMap = mutableMapOf<UUID, UUID?>()

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player

        if (event.from.chunk == event.to.chunk)
            return

        val chunkKey = "${event.to.chunk.x},${event.to.chunk.z}"
        val campement = campementService.getCampementByChunk(chunkKey)
        val newOwnerId = campement?.ownerID
        val lastOwnerId = lastCampementMap[player.uniqueId]
        if (lastOwnerId == newOwnerId)
            return

        lastCampementMap[player.uniqueId] = newOwnerId

        if (newOwnerId == null) {
            player.sendMessage("§a[Nature]")
        } else {
            val ownerName = Bukkit.getOfflinePlayer(newOwnerId).name ?: "Inconnu"
            player.sendMessage("§6[Campement de $ownerName]")
        }
    }
}

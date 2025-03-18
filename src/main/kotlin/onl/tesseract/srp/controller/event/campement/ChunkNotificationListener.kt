package onl.tesseract.srp.controller.event.campement

import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.springframework.stereotype.Component
import org.springframework.context.annotation.Lazy
import java.util.*

/**
 * Displays the camp name or "Nature" depending on the chunk the player moves into.
 */
@Component
open class ChunkNotificationListener(@Lazy private val campementService: CampementService) : Listener {

    private val lastCampementMap = mutableMapOf<UUID, UUID?>()

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player

        if (event.from.chunk == event.to.chunk)
            return

        updatePlayerCampementCache(player.uniqueId, event.to.chunk.x, event.to.chunk.z, notify = true)
    }

    /**
     * Updates the player's camp cache after a claim/unclaim.
     * @param notify If true, displays the camp notification.
     */
    open fun updatePlayerCampementCache(playerId: UUID, chunkX: Int, chunkZ: Int, notify: Boolean) {
        val chunkKey = "$chunkX,$chunkZ"
        val campement = campementService.getCampementByChunk(chunkKey)
        val newOwnerId = campement?.ownerID
        val lastOwnerId = lastCampementMap[playerId]

        if (notify && lastOwnerId == newOwnerId) return

        lastCampementMap[playerId] = newOwnerId

        if (notify) {
            val player = Bukkit.getPlayer(playerId) ?: return
            if (newOwnerId == null) {
                player.sendMessage("§a[Nature]")
            } else {
                val ownerName = Bukkit.getOfflinePlayer(newOwnerId).name ?: "Inconnu"
                player.sendMessage("§6[Campement de $ownerName]")
            }
        }
    }
}


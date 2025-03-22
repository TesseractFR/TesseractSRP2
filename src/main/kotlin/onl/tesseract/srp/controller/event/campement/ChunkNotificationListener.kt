package onl.tesseract.srp.controller.event.campement

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.lib.util.ChatFormats
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.springframework.stereotype.Component as SpringComponent
import org.springframework.context.annotation.Lazy
import java.util.*

/**
 * Displays the camp name or "Nature" depending on the chunk the player moves into.
 */
@SpringComponent
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
                player.sendMessage(Component.text("[Nature]", NamedTextColor.GREEN))
            } else {
                val ownerName = Bukkit.getOfflinePlayer(newOwnerId).name ?: "Inconnu"
                player.sendMessage(Component.text("[Campement de $ownerName]", NamedTextColor.GOLD))
            }
        }
    }
}


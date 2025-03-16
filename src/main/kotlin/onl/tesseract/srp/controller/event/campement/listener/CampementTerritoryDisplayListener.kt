package onl.tesseract.srp.controller.event.campement.listener

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.srp.controller.event.campement.CampementChunkClaimEvent
import onl.tesseract.srp.controller.event.campement.CampementChunkUnclaimEvent
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import java.util.*
import org.springframework.stereotype.Component as SpringComponent

/**
 * Displays the camp name or "Nature" depending on the chunk the player moves into.
 */
@SpringComponent
open class CampementTerritoryDisplayListener(private val campementService: CampementService) : Listener {

    private val lastCampementMap = mutableMapOf<UUID, UUID>()

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        if (event.from.chunk == event.to.chunk)
            return
        updatePlayerCampementCache(player.uniqueId, event.to.chunk.x, event.to.chunk.z, notify = true)
    }

    @EventHandler
    fun onChunkClaim(event: CampementChunkClaimEvent) {
        updatePlayerCampementCache(
            event.playerId,
            event.chunk.x,
            event.chunk.z,
            notify = false
        )
    }

    @EventHandler
    fun onChunkUnclaim(event: CampementChunkUnclaimEvent) {
        updatePlayerCampementCache(
            event.playerId,
            event.chunk.x,
            event.chunk.z,
            notify = false
        )
    }

    /**
     * Updates the player's camp cache after a claim/unclaim.
     * @param notify If true, displays the camp notification.
     */
    open fun updatePlayerCampementCache(playerId: UUID, chunkX: Int, chunkZ: Int, notify: Boolean) {
        val campement = campementService.getCampementByChunk(chunkX, chunkZ)
        val newOwnerId = campement?.ownerID
        val lastOwnerId = lastCampementMap[playerId]

        if (lastOwnerId == newOwnerId) return

        if (newOwnerId == null)
            lastCampementMap.remove(playerId)
        else
            lastCampementMap[playerId] = newOwnerId

        if (notify) {
            val player = Bukkit.getPlayer(playerId) ?: return
            if (newOwnerId == null) {
                player.sendActionBar(Component.text("[Nature]", NamedTextColor.GREEN))
            } else {
                val ownerName = Bukkit.getOfflinePlayer(newOwnerId).name ?: "Inconnu"
                player.sendActionBar(Component.text("[Campement de $ownerName]", NamedTextColor.GOLD))
            }
        }
    }
}


package onl.tesseract.srp.controller.event.territory

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.srp.domain.commun.ChunkCoord
import onl.tesseract.srp.domain.territory.campement.Campement
import onl.tesseract.srp.domain.territory.event.TerritoryClaimEvent
import onl.tesseract.srp.domain.territory.event.TerritoryUnclaimEvent
import onl.tesseract.srp.domain.territory.guild.Guild
import onl.tesseract.srp.mapper.toChunkCoord
import onl.tesseract.srp.service.territory.campement.CampementService
import onl.tesseract.srp.service.territory.guild.GuildService
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.springframework.context.event.EventListener
import java.util.*
import org.springframework.stereotype.Component as SpringComponent

/**
 * Displays the camp name or "Nature" depending on the chunk the player moves into.
 */
@SpringComponent
open class TerritoryDisplayListener(private val campementService: CampementService,
    private val guildService: GuildService) : Listener {

    private val lastTerritory = mutableMapOf<UUID, UUID>()

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        if (event.from.chunk == event.to.chunk)
            return
        updatePlayerDisplay(player,event.to.chunk.toChunkCoord())
    }

    @EventListener
    fun onChunkClaim(event: TerritoryClaimEvent<*>) {
        val p = Bukkit.getPlayer(event.playerId)?:return
        updatePlayerDisplay(p,p.chunk.toChunkCoord())
    }

    @EventListener
    fun onChunkUnclaim(event: TerritoryUnclaimEvent<*>) {
        val p = Bukkit.getPlayer(event.playerId)?:return
        updatePlayerDisplay(p,p.chunk.toChunkCoord())
    }

    open fun updatePlayerDisplay(player: Player, toChunkCoord: ChunkCoord) {
        val campement: Campement? = campementService.getByChunk(toChunkCoord)
        val guild: Guild? = guildService.getByChunk(toChunkCoord)
        val territory: UUID? = campement?.id ?: guild?.id
        if (territory == null) {
            if (lastTerritory.keys.contains(player.uniqueId)) {
                lastTerritory.remove(player.uniqueId)
                player.sendActionBar(Component.text("[Nature]", NamedTextColor.GREEN))
            }
            return
        }
        if(!lastTerritory.keys.contains(player.uniqueId) || lastTerritory[player.uniqueId] != territory){
            lastTerritory[player.uniqueId] = territory
            when {
                campement != null -> {
                    val ownerName = Bukkit.getOfflinePlayer(campement.ownerID).name ?: "Inconnu"
                    player.sendActionBar(Component.text("[Campement de $ownerName]", NamedTextColor.GOLD))
                }
                guild != null -> {
                    player.sendActionBar(Component.text("[${guild.name}]", NamedTextColor.GOLD))
                }
            }
        }
    }
}


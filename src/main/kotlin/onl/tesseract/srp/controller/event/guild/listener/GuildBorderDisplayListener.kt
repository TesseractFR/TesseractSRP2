package onl.tesseract.srp.controller.event.guild.listener

import onl.tesseract.srp.domain.territory.guild.event.GuildChunkClaimEvent
import onl.tesseract.srp.domain.territory.guild.event.GuildChunkUnclaimEvent
import onl.tesseract.srp.service.territory.guild.GuildBorderRenderer
import onl.tesseract.srp.service.territory.guild.GuildService
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class GuildBorderDisplayListener(
    private val borderRenderer: GuildBorderRenderer,
    private val guildService: GuildService
) : Listener {

    @EventListener
    fun onChunkClaim(event: GuildChunkClaimEvent) = updateBorders(event.playerId)

    @EventListener
    fun onChunkUnclaim(event: GuildChunkUnclaimEvent) = updateBorders(event.playerId)

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) = borderRenderer.clearBorders(event.player.uniqueId)

    @EventHandler
    fun onKick(event: PlayerKickEvent) = borderRenderer.clearBorders(event.player.uniqueId)

    private fun updateBorders(playerId: java.util.UUID) {
        val player = Bukkit.getPlayer(playerId)
        val guild = guildService.getGuildByMember(playerId)
        if (player != null && guild != null && borderRenderer.isShowingBorders(player.uniqueId)) {
            borderRenderer.showBorders(player.uniqueId)
        }
    }

}

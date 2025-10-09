package onl.tesseract.srp.controller.event.guild.listener

import onl.tesseract.srp.controller.event.guild.GuildChunkClaimEvent
import onl.tesseract.srp.controller.event.guild.GuildChunkUnclaimEvent
import onl.tesseract.srp.service.guild.GuildBorderRenderer
import onl.tesseract.srp.service.guild.GuildService
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.springframework.stereotype.Component

@Component
class GuildBorderDisplayListener(
    private val borderRenderer: GuildBorderRenderer,
    private val guildService: GuildService
) : Listener {

    @EventHandler
    fun onChunkClaim(event: GuildChunkClaimEvent) = updateBorders(event.playerId)

    @EventHandler
    fun onChunkUnclaim(event: GuildChunkUnclaimEvent) = updateBorders(event.playerId)

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) = borderRenderer.clearBorders(event.player)

    @EventHandler
    fun onKick(event: PlayerKickEvent) = borderRenderer.clearBorders(event.player)

    private fun updateBorders(playerId: java.util.UUID) {
        val player = Bukkit.getPlayer(playerId) ?: return
        val guild = guildService.getGuildByMember(playerId) ?: return
        if (!borderRenderer.isShowingBorders(player)) return
        borderRenderer.showBorders(player, guild.chunks)
    }
}

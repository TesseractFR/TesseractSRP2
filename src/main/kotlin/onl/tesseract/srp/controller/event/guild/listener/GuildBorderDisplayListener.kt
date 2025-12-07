package onl.tesseract.srp.controller.event.guild.listener

import onl.tesseract.srp.controller.event.territory.listener.TerritoryBorderDisplayListener
import onl.tesseract.srp.domain.territory.guild.Guild
import onl.tesseract.srp.domain.territory.guild.GuildChunk
import onl.tesseract.srp.domain.territory.guild.event.GuildChunkClaimEvent
import onl.tesseract.srp.domain.territory.guild.event.GuildChunkUnclaimEvent
import onl.tesseract.srp.service.territory.guild.GuildBorderService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class GuildBorderDisplayListener(
    guildBorderService: GuildBorderService
) : TerritoryBorderDisplayListener<GuildChunk, Guild>(guildBorderService) {

    @EventListener
    fun onChunkClaim(event: GuildChunkClaimEvent) =
        updateBorders(event.playerId)

    @EventListener
    fun onChunkUnclaim(event: GuildChunkUnclaimEvent) =
        updateBorders(event.playerId)
}

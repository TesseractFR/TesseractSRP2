package onl.tesseract.srp.controller.event.campement.listener

import onl.tesseract.srp.domain.player.event.PlayerRankUpEvent
import onl.tesseract.srp.service.territory.campement.CampementService
import org.bukkit.event.Listener
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
open class CampLevelUpdateListener(
    private val campementService: CampementService
) : Listener {

    @EventListener
    fun onPlayerRankUpdate(event: PlayerRankUpEvent) {
        campementService.setCampLevel(event.playerId, event.newRank.campLevel)
    }
}

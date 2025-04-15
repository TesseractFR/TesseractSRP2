package onl.tesseract.srp.controller.event.campement.listener

import onl.tesseract.srp.controller.event.campement.CampLevelUpdateEvent
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
open class CampLevelUpdateListener(
    private val campementService: CampementService
) : Listener {

    @EventHandler
    fun onPlayerRankUpdate(event: CampLevelUpdateEvent) {
        val playerId = event.playerId
        val newLevel = event.newRank.campLevel
        campementService.setCampLevel(playerId, newLevel)
    }
}

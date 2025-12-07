package onl.tesseract.srp.controller.event.campement.listener

import onl.tesseract.srp.controller.event.territory.TerritoryBorderDisplayListener
import onl.tesseract.srp.domain.territory.campement.Campement
import onl.tesseract.srp.domain.territory.campement.CampementChunk
import onl.tesseract.srp.domain.territory.campement.CampementChunkClaimEvent
import onl.tesseract.srp.domain.territory.campement.CampementChunkUnclaimEvent
import onl.tesseract.srp.service.territory.campement.CampementBorderService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class CampementBorderDisplayListener(
    campementBorderService: CampementBorderService
) : TerritoryBorderDisplayListener<CampementChunk, Campement>(campementBorderService) {

    @EventListener
    fun onChunkClaim(event: CampementChunkClaimEvent) =
        updateBorders(event.playerId)

    @EventListener
    fun onChunkUnclaim(event: CampementChunkUnclaimEvent) =
        updateBorders(event.playerId)
}

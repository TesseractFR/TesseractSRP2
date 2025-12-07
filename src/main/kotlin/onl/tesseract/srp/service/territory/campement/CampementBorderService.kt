package onl.tesseract.srp.service.territory.campement

import onl.tesseract.srp.domain.territory.campement.Campement
import onl.tesseract.srp.domain.territory.campement.CampementChunk
import onl.tesseract.srp.infrastructure.scheduler.territory.TerritoryBorderTaskScheduler
import onl.tesseract.srp.service.territory.TerritoryBorderService
import org.springframework.stereotype.Component

@Component
open class CampementBorderService(
    override val scheduler: TerritoryBorderTaskScheduler,
    override val territoryService: CampementService,
) : TerritoryBorderService<CampementChunk, Campement>()


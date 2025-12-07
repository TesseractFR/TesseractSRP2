package onl.tesseract.srp.service.territory.guild

import onl.tesseract.srp.domain.territory.guild.Guild
import onl.tesseract.srp.domain.territory.guild.GuildChunk
import onl.tesseract.srp.infrastructure.scheduler.territory.TerritoryBorderTaskScheduler
import onl.tesseract.srp.service.territory.TerritoryBorderService
import org.springframework.stereotype.Component

@Component
class GuildBorderService(
    override val scheduler: TerritoryBorderTaskScheduler,
    override val territoryService: GuildService,
) : TerritoryBorderService<GuildChunk, Guild>()

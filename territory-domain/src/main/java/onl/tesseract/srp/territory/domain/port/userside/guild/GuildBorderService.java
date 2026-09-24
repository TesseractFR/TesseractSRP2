package onl.tesseract.srp.territory.domain.port.userside.guild;


import onl.tesseract.srp.territory.domain.model.guild.Guild;
import onl.tesseract.srp.territory.domain.model.guild.GuildChunk;
import onl.tesseract.srp.territory.domain.port.serverside.TerritoryBorderTaskScheduler;
import onl.tesseract.srp.territory.domain.port.userside.TerritoryBorderService;

public class GuildBorderService extends TerritoryBorderService<GuildChunk, Guild> {
    public GuildBorderService(
            TerritoryBorderTaskScheduler scheduler,
            GuildService territoryService) {
        super(scheduler, territoryService);
    }
}


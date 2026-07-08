package onl.tesseract.srp.skill.adapter.serverside.jpa;

import onl.tesseract.srp.domain.commun.ChunkCoord;
import onl.tesseract.srp.domain.territory.guild.Guild;
import onl.tesseract.srp.service.territory.guild.GuildService;
import onl.tesseract.srp.skill.domain.port.serverside.TerritoryRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GuildTerritoryRepository implements TerritoryRepository {

    private final GuildService guildService;

    public GuildTerritoryRepository(GuildService guildService) {
        this.guildService = guildService;
    }

    @Override
    public UUID get(ChunkCoord coord) {
        Guild guild = guildService.getByChunk(coord);
        if(guild == null) return null;
        return guild.getId();
    }
}

package onl.tesseract.srp.skill.adapter.serverside.jpa;

import onl.tesseract.srp.common.domain.model.ChunkCoord;
import onl.tesseract.srp.skill.domain.port.serverside.TerritoryRepository;
import onl.tesseract.srp.territory.domain.model.guild.Guild;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildService;
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
        if (guild == null) return null;
        return guild.getId();
    }
}

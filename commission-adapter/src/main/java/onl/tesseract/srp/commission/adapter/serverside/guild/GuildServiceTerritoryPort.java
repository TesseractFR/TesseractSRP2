package onl.tesseract.srp.commission.adapter.serverside.guild;

import onl.tesseract.srp.commission.domain.model.board.GuildId;
import onl.tesseract.srp.commission.domain.port.serverside.GuildTerritoryPort;
import onl.tesseract.srp.common.domain.model.ChunkCoord;
import onl.tesseract.srp.territory.domain.model.guild.Guild;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildService;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Lit le territoire des guildes au moyen de {@link GuildService}. */
@Component
public class GuildServiceTerritoryPort implements GuildTerritoryPort {

    private final GuildService guildService;

    public GuildServiceTerritoryPort(GuildService guildService) {
        this.guildService = guildService;
    }

    @Override
    public boolean ownsChunk(GuildId guildId, ChunkCoord chunk) {
        return Optional.ofNullable(guildService.getByChunk(chunk))
                .map(Guild::getId)
                .filter(guildId.value()::equals)
                .isPresent();
    }
}

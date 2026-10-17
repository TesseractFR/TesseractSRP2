package onl.tesseract.srp.territory.domain.port.userside.guild;


import onl.tesseract.srp.territory.domain.model.guild.Guild;
import onl.tesseract.srp.territory.domain.model.guild.GuildChunk;
import onl.tesseract.srp.territory.domain.port.serverside.PlayerGuildMessagingRepository;
import onl.tesseract.srp.territory.domain.port.serverside.TerritoryBorderTaskScheduler;
import onl.tesseract.srp.territory.domain.port.userside.TerritoryBorderService;

import java.util.UUID;

public class GuildBorderService extends TerritoryBorderService<GuildChunk, Guild> {
    private final PlayerGuildMessagingRepository messaging;

    public GuildBorderService(
            TerritoryBorderTaskScheduler scheduler,
            GuildService territoryService,
            PlayerGuildMessagingRepository messaging) {
        super(scheduler, territoryService);
        this.messaging = messaging;
    }

    public void toggle(UUID player, String world) {
        switch (toggleBorders(player, world)) {
            case SHOW_BORDERS -> messaging.playerBordersShown(player);
            case CLEAR_BORDERS -> messaging.playerBordersHidden(player);
            case TERRITORY_NOT_FOUND -> messaging.playerNoGuild(player);
            case INVALID_WORLD -> messaging.playerInvalidWorld(player);
        }
    }
}


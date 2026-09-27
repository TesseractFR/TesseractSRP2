package onl.tesseract.srp.territory.adapter.userside.event.guild;

import onl.tesseract.srp.territory.adapter.userside.event.territory.TerritoryBorderDisplayListener;
import onl.tesseract.srp.territory.domain.model.guild.Guild;
import onl.tesseract.srp.territory.domain.model.guild.GuildChunk;
import onl.tesseract.srp.territory.domain.model.guild.event.GuildChunkClaimEvent;
import onl.tesseract.srp.territory.domain.model.guild.event.GuildChunkUnclaimEvent;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildBorderService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class GuildBorderDisplayListener extends TerritoryBorderDisplayListener<GuildChunk, Guild> {

    public GuildBorderDisplayListener(GuildBorderService guildBorderService) {
        super(guildBorderService);
    }

    @EventListener
    public void onChunkClaim(GuildChunkClaimEvent event) {
        updateBorders(event.getPlayerId());
    }

    @EventListener
    public void onChunkUnclaim(GuildChunkUnclaimEvent event) {
        updateBorders(event.getPlayerId());
    }
}

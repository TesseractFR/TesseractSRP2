package onl.tesseract.srp.territory.domain.model.guild.event;

import onl.tesseract.srp.territory.domain.model.guild.GuildChunk;
import onl.tesseract.srp.territory.domain.model.event.TerritoryClaimEvent;
import java.util.*;

public class GuildChunkClaimEvent extends TerritoryClaimEvent<GuildChunk> {
    public GuildChunkClaimEvent(UUID playerId){
        super(playerId);
    }
}

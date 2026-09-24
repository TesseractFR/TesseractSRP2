package onl.tesseract.srp.territory.domain.model.guild.event;

import onl.tesseract.srp.territory.domain.model.guild.GuildChunk;
import onl.tesseract.srp.territory.domain.model.event.TerritoryUnclaimEvent;
import java.util.*;

public class GuildChunkUnclaimEvent extends TerritoryUnclaimEvent<GuildChunk> {
    public GuildChunkUnclaimEvent(UUID playerId) {
        super(playerId);
    }
}

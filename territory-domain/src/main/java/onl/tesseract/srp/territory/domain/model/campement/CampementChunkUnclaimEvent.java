package onl.tesseract.srp.territory.domain.model.campement;

import onl.tesseract.srp.territory.domain.model.event.TerritoryUnclaimEvent;

import java.util.UUID;

public class CampementChunkUnclaimEvent extends TerritoryUnclaimEvent<CampementChunk> {

    public CampementChunkUnclaimEvent(UUID playerId) {
        super(playerId);
    }
}


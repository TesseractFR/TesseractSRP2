package onl.tesseract.srp.territory.domain.model.campement;

import onl.tesseract.srp.territory.domain.model.event.TerritoryClaimEvent;

import java.util.UUID;

public class CampementChunkClaimEvent extends TerritoryClaimEvent<CampementChunk> {

    public CampementChunkClaimEvent(UUID playerId) {
        super(playerId);
    }
}


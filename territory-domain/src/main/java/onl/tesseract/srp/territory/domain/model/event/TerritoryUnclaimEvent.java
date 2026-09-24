package onl.tesseract.srp.territory.domain.model.event;

import onl.tesseract.srp.territory.domain.model.TerritoryChunk;

import java.util.UUID;

public abstract class TerritoryUnclaimEvent<TC extends TerritoryChunk> {

    private final UUID playerId;

    protected TerritoryUnclaimEvent(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID getPlayerId() {
        return playerId;
    }
}


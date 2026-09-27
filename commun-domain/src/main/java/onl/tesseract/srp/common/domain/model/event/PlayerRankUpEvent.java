package onl.tesseract.srp.common.domain.model.event;

import onl.tesseract.srp.common.domain.model.enums.PlayerRank;

import java.util.UUID;

public class PlayerRankUpEvent {
    private final UUID playerId;
    private final PlayerRank newRank;

    public PlayerRankUpEvent(UUID playerId, PlayerRank newRank) {
        this.playerId = playerId;
        this.newRank = newRank;
    }

    public UUID getPlayerId() { return playerId; }
    public PlayerRank getNewRank() { return newRank; }
}


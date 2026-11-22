package onl.tesseract.srp.player.domain.model.event;

import onl.tesseract.srp.player.domain.model.PlayerRank;

import java.util.UUID;

/** Le grade du joueur a changé : achat du grade suivant, ou grade donné par le staff, qui peut être inférieur. */
public class PlayerRankChangedEvent {
    private final UUID playerId;
    private final PlayerRank newRank;

    public PlayerRankChangedEvent(UUID playerId, PlayerRank newRank) {
        this.playerId = playerId;
        this.newRank = newRank;
    }

    public UUID getPlayerId() { return playerId; }
    public PlayerRank getNewRank() { return newRank; }
}


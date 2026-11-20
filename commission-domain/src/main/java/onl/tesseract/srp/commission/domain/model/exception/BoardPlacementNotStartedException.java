package onl.tesseract.srp.commission.domain.model.exception;

import onl.tesseract.srp.common.domain.model.PlayerId;

/** Le joueur ne pose pas de tableau en ce moment. */
public final class BoardPlacementNotStartedException extends CommissionException {
    private final PlayerId player;

    public BoardPlacementNotStartedException(PlayerId player) {
        super("Le joueur " + player.value() + " ne pose pas de tableau");
        this.player = player;
    }

    public PlayerId getPlayer() {
        return player;
    }
}

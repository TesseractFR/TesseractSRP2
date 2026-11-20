package onl.tesseract.srp.commission.domain.model.exception;

import onl.tesseract.srp.common.domain.model.PlayerId;

/** Le joueur ne peut pas gérer ce tableau : il n'est pas Adjoint ou chef de sa guilde, ou le tableau est celui du spawn. */
public final class BoardManagementNotAllowedException extends CommissionException {
    private final PlayerId player;

    public BoardManagementNotAllowedException(PlayerId player) {
        super("Le joueur " + player.value() + " ne peut pas gérer ce tableau");
        this.player = player;
    }

    public PlayerId getPlayer() {
        return player;
    }
}

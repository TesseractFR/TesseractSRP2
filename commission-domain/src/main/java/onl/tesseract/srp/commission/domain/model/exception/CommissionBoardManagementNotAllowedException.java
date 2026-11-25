package onl.tesseract.srp.commission.domain.model.exception;

import lombok.Getter;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardId;
import onl.tesseract.srp.common.domain.model.PlayerId;

/** Le joueur ne peut pas gérer ce tableau : il n'est pas Adjoint ou chef de sa guilde, ou le tableau est celui du spawn. */
@Getter
public final class CommissionBoardManagementNotAllowedException extends CommissionException {
    private final PlayerId player;
    private final CommissionBoardId boardId;

    public CommissionBoardManagementNotAllowedException(PlayerId player, CommissionBoardId boardId) {
        super("Le joueur " + player.value() + " ne peut pas gérer le tableau " + boardId.value());
        this.player = player;
        this.boardId = boardId;
    }
}

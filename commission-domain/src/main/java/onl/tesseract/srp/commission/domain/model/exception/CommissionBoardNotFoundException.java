package onl.tesseract.srp.commission.domain.model.exception;

import lombok.Getter;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardId;

/** Le tableau demandé n'existe pas. */
@Getter
public final class CommissionBoardNotFoundException extends CommissionException {
    private final CommissionBoardId boardId;

    public CommissionBoardNotFoundException(CommissionBoardId boardId) {
        super("Le tableau " + boardId.value() + " n'existe pas");
        this.boardId = boardId;
    }
}

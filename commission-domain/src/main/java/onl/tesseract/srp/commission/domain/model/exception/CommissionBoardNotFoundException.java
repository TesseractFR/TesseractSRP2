package onl.tesseract.srp.commission.domain.model.exception;

import onl.tesseract.srp.commission.domain.model.board.BoardId;

/** Le tableau demandé n'existe pas. */
public final class CommissionBoardNotFoundException extends CommissionException {
    private final BoardId boardId;

    public CommissionBoardNotFoundException(BoardId boardId) {
        super("Le tableau " + boardId.value() + " n'existe pas");
        this.boardId = boardId;
    }

    public BoardId getBoardId() {
        return boardId;
    }
}

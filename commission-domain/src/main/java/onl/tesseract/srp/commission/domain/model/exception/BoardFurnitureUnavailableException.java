package onl.tesseract.srp.commission.domain.model.exception;

import onl.tesseract.srp.commission.domain.model.board.BoardSpot;

/** Le tableau n'a pas pu être posé, par exemple parce que l'item n'est pas chargé. */
public final class BoardFurnitureUnavailableException extends CommissionException {
    private final BoardSpot boardSpot;

    public BoardFurnitureUnavailableException(BoardSpot boardSpot) {
        super("Le tableau n'a pas pu être posé en " + boardSpot.centerBlock());
        this.boardSpot = boardSpot;
    }

    public BoardSpot getBoardSpot() {
        return boardSpot;
    }
}

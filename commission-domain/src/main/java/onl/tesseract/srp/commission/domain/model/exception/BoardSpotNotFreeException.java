package onl.tesseract.srp.commission.domain.model.exception;

import onl.tesseract.srp.commission.domain.model.board.BoardSpot;

/** Les blocs de l'emplacement choisi pour le tableau ne sont pas libres. */
public final class BoardSpotNotFreeException extends CommissionException {
    private final BoardSpot boardSpot;

    public BoardSpotNotFreeException(BoardSpot boardSpot) {
        super("L'emplacement " + boardSpot.centerBlock() + " n'est pas libre");
        this.boardSpot = boardSpot;
    }

    public BoardSpot getBoardSpot() {
        return boardSpot;
    }
}

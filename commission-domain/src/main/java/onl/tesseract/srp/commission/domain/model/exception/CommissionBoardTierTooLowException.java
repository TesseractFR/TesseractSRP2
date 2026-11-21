package onl.tesseract.srp.commission.domain.model.exception;

import lombok.Getter;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardId;

/** Le tier du tableau est trop bas pour le palier d'amélioration. */
@Getter
public final class CommissionBoardTierTooLowException extends CommissionException {
    private final CommissionBoardId boardId;
    private final int requiredBoardTier;
    private final int boardTier;

    public CommissionBoardTierTooLowException(CommissionBoardId boardId, int requiredBoardTier, int boardTier) {
        super("Le tableau " + boardId.value() + " est tier " + boardTier + ", il faut le tier " + requiredBoardTier);
        this.boardId = boardId;
        this.requiredBoardTier = requiredBoardTier;
        this.boardTier = boardTier;
    }
}

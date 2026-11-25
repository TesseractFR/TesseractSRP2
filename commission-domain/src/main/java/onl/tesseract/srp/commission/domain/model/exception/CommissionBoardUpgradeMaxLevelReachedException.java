package onl.tesseract.srp.commission.domain.model.exception;

import lombok.Getter;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardId;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardUpgradeType;

/** L'amélioration du tableau est déjà au niveau maximal. */
@Getter
public final class CommissionBoardUpgradeMaxLevelReachedException extends CommissionException {
    private final CommissionBoardId boardId;
    private final CommissionBoardUpgradeType upgradeType;

    public CommissionBoardUpgradeMaxLevelReachedException(CommissionBoardId boardId, CommissionBoardUpgradeType upgradeType) {
        super("L'amélioration " + upgradeType + " du tableau " + boardId.value() + " est déjà au niveau maximal");
        this.boardId = boardId;
        this.upgradeType = upgradeType;
    }
}

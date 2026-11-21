package onl.tesseract.srp.commission.domain.model.exception;

import lombok.Getter;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardUpgradeType;

/** L'amélioration est déjà au niveau maximal. */
@Getter
public final class CommissionBoardUpgradeMaxLevelReachedException extends CommissionException {
    private final CommissionBoardUpgradeType upgradeType;

    public CommissionBoardUpgradeMaxLevelReachedException(CommissionBoardUpgradeType upgradeType) {
        super("L'amélioration " + upgradeType + " est déjà au niveau maximal");
        this.upgradeType = upgradeType;
    }
}

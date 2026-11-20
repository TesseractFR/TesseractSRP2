package onl.tesseract.srp.commission.domain.model.exception;

import onl.tesseract.srp.commission.domain.model.board.UpgradeType;

/** L'amélioration est déjà au niveau maximal. */
public final class UpgradeMaxLevelReachedException extends CommissionException {
    private final UpgradeType upgradeType;

    public UpgradeMaxLevelReachedException(UpgradeType upgradeType) {
        super("L'amélioration " + upgradeType + " est déjà au niveau maximal");
        this.upgradeType = upgradeType;
    }

    public UpgradeType getUpgradeType() {
        return upgradeType;
    }
}

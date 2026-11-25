package onl.tesseract.srp.commission.adapter.userside.controller.message;

import onl.tesseract.srp.commission.domain.model.board.CommissionBoardUpgradeType;
import onl.tesseract.srp.commission.domain.model.board.UsageTaxRate;
import onl.tesseract.srp.commission.domain.model.commission.CommissionType;

/** Libellés des notions de commission, communs aux messages du chat et aux menus. */
public final class CommissionLabels {

    private CommissionLabels() {
    }

    public static String formatCommissionType(CommissionType commissionType) {
        return commissionType == CommissionType.HARVEST ? "Récolte" : "Artisanat";
    }

    public static String formatUpgradeName(CommissionBoardUpgradeType upgradeType) {
        return switch (upgradeType) {
            case BOARD_TIER -> "Tier du tableau";
            case MAX_HARVEST_TIER -> "Tier de récolte";
            case MAX_CRAFT_TIER -> "Tier d'artisanat";
            case HARVEST_SLOTS -> "Emplacements de récolte";
            case CRAFT_SLOTS -> "Emplacements d'artisanat";
            case MONEY_BONUS -> "Bonus d'argent";
        };
    }

    public static String formatUsageTax(UsageTaxRate usageTaxRate) {
        return usageTaxRate.percent() + " %";
    }

    /** @param rate taux décimal, 0,5 pour 50 % */
    public static String formatPercent(double rate) {
        return Math.round(rate * 100) + " %";
    }
}

package onl.tesseract.srp.commission.domain.model.board;

import onl.tesseract.srp.commission.domain.model.commission.CommissionType;

/**
 * Niveaux d'amélioration d'un tableau.
 *
 * @param boardTier       tier du tableau, qui débloque les paliers des autres améliorations
 * @param maxHarvestTier  tier maximal des items de récolte demandés
 * @param maxCraftTier    tier maximal des recettes demandées
 * @param harvestSlots    nombre de commissions de récolte par partie
 * @param craftSlots      nombre de commissions d'artisanat par partie
 * @param moneyBonusLevel niveau du bonus d'argent, dont le taux vient de la configuration
 */
public record CommissionBoardLevels(int boardTier, int maxHarvestTier, int maxCraftTier, int harvestSlots, int craftSlots,
                          int moneyBonusLevel) {
    /** Niveaux de départ d'un tableau de guilde : tout au premier niveau, une commission par type et par partie. */
    public static final CommissionBoardLevels GUILD_BOARD_START = new CommissionBoardLevels(1, 1, 1, 1, 1, 0);

    /** @return niveaux du tableau du spawn, qui n'est pas améliorable : tiers à 1, sans bonus */
    public static CommissionBoardLevels forSpawnBoard(int slotsPerType) {
        return new CommissionBoardLevels(1, 1, 1, slotsPerType, slotsPerType, 0);
    }

    /** @return tier maximal des items demandés pour ce type de commission */
    public int getMaxTierFor(CommissionType commissionType) {
        return commissionType == CommissionType.CRAFT ? maxCraftTier : maxHarvestTier;
    }

    /** @return nombre de commissions de ce type dans chaque partie du tableau */
    public int getSlotsFor(CommissionType commissionType) {
        return commissionType == CommissionType.CRAFT ? craftSlots : harvestSlots;
    }

    /** @return niveau actuel de l'amélioration */
    public int getLevel(CommissionBoardUpgradeType upgradeType) {
        return switch (upgradeType) {
            case BOARD_TIER -> boardTier;
            case MAX_HARVEST_TIER -> maxHarvestTier;
            case MAX_CRAFT_TIER -> maxCraftTier;
            case HARVEST_SLOTS -> harvestSlots;
            case CRAFT_SLOTS -> craftSlots;
            case MONEY_BONUS -> moneyBonusLevel;
        };
    }

    /** @return les niveaux avec cette amélioration montée d'un palier */
    public CommissionBoardLevels withUpgrade(CommissionBoardUpgradeType upgradeType) {
        return switch (upgradeType) {
            case BOARD_TIER -> new CommissionBoardLevels(boardTier + 1, maxHarvestTier, maxCraftTier,
                    harvestSlots, craftSlots, moneyBonusLevel);
            case MAX_HARVEST_TIER -> new CommissionBoardLevels(boardTier, maxHarvestTier + 1, maxCraftTier,
                    harvestSlots, craftSlots, moneyBonusLevel);
            case MAX_CRAFT_TIER -> new CommissionBoardLevels(boardTier, maxHarvestTier, maxCraftTier + 1,
                    harvestSlots, craftSlots, moneyBonusLevel);
            case HARVEST_SLOTS -> new CommissionBoardLevels(boardTier, maxHarvestTier, maxCraftTier,
                    harvestSlots + 1, craftSlots, moneyBonusLevel);
            case CRAFT_SLOTS -> new CommissionBoardLevels(boardTier, maxHarvestTier, maxCraftTier,
                    harvestSlots, craftSlots + 1, moneyBonusLevel);
            case MONEY_BONUS -> new CommissionBoardLevels(boardTier, maxHarvestTier, maxCraftTier,
                    harvestSlots, craftSlots, moneyBonusLevel + 1);
        };
    }
}

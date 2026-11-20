package onl.tesseract.srp.commission.domain.model.board;

import lombok.NonNull;
import onl.tesseract.srp.commission.domain.model.commission.CommissionType;
import onl.tesseract.srp.common.domain.model.enums.Quality;

/**
 * Niveaux d'amélioration d'un tableau.
 *
 * @param maxCraftTier       tier maximal des recettes demandées
 * @param maxHarvestTier     tier maximal des items de récolte demandés
 * @param slotsPerType       nombre de commissions par type et par partie
 * @param maxRequiredQuality qualité minimale la plus haute qu'une commission peut exiger
 * @param moneyBonusLevel    niveau du bonus d'argent, dont le taux vient de la configuration
 */
public record BoardLevels(int maxCraftTier, int maxHarvestTier, int slotsPerType, @NonNull Quality maxRequiredQuality,
                          int moneyBonusLevel) {
    /** Niveaux de départ d'un tableau de guilde : un slot par type et par partie. */
    public static final BoardLevels GUILD_START = new BoardLevels(1, 1, 1, Quality.POOR, 0);

    /** Vérifie que les tiers et les slots valent au moins un et que le bonus n'est pas négatif. */
    public BoardLevels {
        if (maxCraftTier < 1 || maxHarvestTier < 1) {
            throw new IllegalArgumentException("Les tiers d'un tableau valent au moins 1");
        }
        if (slotsPerType < 1) {
            throw new IllegalArgumentException("Un tableau a au moins un slot par type : " + slotsPerType);
        }
        if (moneyBonusLevel < 0) {
            throw new IllegalArgumentException("Le niveau de bonus ne peut pas être négatif : " + moneyBonusLevel);
        }
    }

    /** @return niveaux du tableau du spawn, qui n'est pas améliorable : tiers à 1, qualité {@code POOR}, sans bonus */
    public static BoardLevels spawn(int slotsPerType) {
        return new BoardLevels(1, 1, slotsPerType, Quality.POOR, 0);
    }

    /** @return tier maximal des items demandés pour ce type de commission */
    public int getMaxTierFor(CommissionType commissionType) {
        return commissionType == CommissionType.CRAFT ? maxCraftTier : maxHarvestTier;
    }

    /** @return niveau actuel de l'amélioration ; pour la qualité, son rang de {@code POOR} (0) à {@code EXCEPTIONAL} (4) */
    public int getLevel(UpgradeType upgradeType) {
        return switch (upgradeType) {
            case MAX_CRAFT_TIER -> maxCraftTier;
            case MAX_HARVEST_TIER -> maxHarvestTier;
            case SLOTS -> slotsPerType;
            case MAX_REQUIRED_QUALITY -> maxRequiredQuality.ordinal();
            case MONEY_BONUS -> moneyBonusLevel;
        };
    }

    /** @return les niveaux avec cette amélioration montée d'un palier */
    public BoardLevels upgrade(UpgradeType upgradeType) {
        return switch (upgradeType) {
            case MAX_CRAFT_TIER -> new BoardLevels(maxCraftTier + 1, maxHarvestTier, slotsPerType, maxRequiredQuality, moneyBonusLevel);
            case MAX_HARVEST_TIER -> new BoardLevels(maxCraftTier, maxHarvestTier + 1, slotsPerType, maxRequiredQuality, moneyBonusLevel);
            case SLOTS -> new BoardLevels(maxCraftTier, maxHarvestTier, slotsPerType + 1, maxRequiredQuality, moneyBonusLevel);
            case MAX_REQUIRED_QUALITY -> new BoardLevels(maxCraftTier, maxHarvestTier, slotsPerType, maxRequiredQuality.next(), moneyBonusLevel);
            case MONEY_BONUS -> new BoardLevels(maxCraftTier, maxHarvestTier, slotsPerType, maxRequiredQuality, moneyBonusLevel + 1);
        };
    }
}

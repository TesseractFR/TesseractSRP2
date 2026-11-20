package onl.tesseract.srp.commission.domain.model.board;

/** Améliorations qu'une guilde peut acheter pour son tableau. */
public enum UpgradeType {
    /** Tier maximal des recettes demandées. */
    MAX_CRAFT_TIER,
    /** Tier maximal des items de récolte demandés. */
    MAX_HARVEST_TIER,
    /** Nombre de commissions par type et par partie. */
    SLOTS,
    /** Qualité minimale la plus haute qu'une commission peut exiger. */
    MAX_REQUIRED_QUALITY,
    /** Bonus d'argent sur les récompenses. */
    MONEY_BONUS
}

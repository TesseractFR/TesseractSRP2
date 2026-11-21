package onl.tesseract.srp.commission.domain.model.board;

/** Améliorations qu'une guilde peut acheter pour son tableau. */
public enum CommissionBoardUpgradeType {
    /** Tier du tableau : l'amélioration centrale, qui débloque les paliers des autres. */
    BOARD_TIER,
    /** Tier maximal des items de récolte demandés. */
    MAX_HARVEST_TIER,
    /** Tier maximal des recettes demandées. */
    MAX_CRAFT_TIER,
    /** Nombre de commissions de récolte, dans la partie publique et dans la partie guilde. */
    HARVEST_SLOTS,
    /** Nombre de commissions d'artisanat, dans la partie publique et dans la partie guilde. */
    CRAFT_SLOTS,
    /** Bonus d'argent sur les récompenses. */
    MONEY_BONUS
}

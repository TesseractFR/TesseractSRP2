package onl.tesseract.srp.domain.job;

/**
 * Bonuses that a job skill can contain
 */
public enum JobBonusType {
    /**
     * Additional money in a mission's reward, percentage of the base gain.
     */
    Money,

    /**
     * Additional loot chance during harvest, as a percentage of the base chance
     */
    LootChance,

    Quality
}


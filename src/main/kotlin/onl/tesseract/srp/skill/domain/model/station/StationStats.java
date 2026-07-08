package onl.tesseract.srp.skill.domain.model.station;

import onl.tesseract.srp.domain.skill.station.StatType;
import onl.tesseract.srp.skill.domain.model.bonus.Bonus;
import onl.tesseract.srp.skill.domain.model.bonus.StationBonus;
import org.jetbrains.annotations.NotNull;

public record StationStats(
        UpgradeLevel tier,
        UpgradeLevel quality,
        UpgradeLevel success,
        UpgradeLevel timeReduction
) {
    public StationStats() {
        this(new UpgradeLevel(1), new UpgradeLevel(0), new UpgradeLevel(0), new UpgradeLevel(0));
    }

    public int getStatLevel(@NotNull StatType statType) {
        return switch (statType) {
            case TIER -> tier.value();
            case QUALITY -> quality.value();
            case SUCCESS -> success.value();
            case TIME_REDUCTION -> timeReduction.value();
            default -> throw new IllegalStateException("Unexpected value: " + statType);
        };
    }

    public StationBonus getBonus() {
        return new StationBonus(
                new Bonus(quality().value()*0.02),
                new Bonus(success().value()*0.015),
                new Bonus(timeReduction().value()*0.01)
        );
    }

    public StationStats upgradeStat(@NotNull StatType statType) {
        return switch (statType) {
            case TIER -> new StationStats(new UpgradeLevel(tier.value() + 1), quality, success, timeReduction);
            case QUALITY -> new StationStats(tier, new UpgradeLevel(quality.value() + 1), success, timeReduction);
            case SUCCESS -> new StationStats(tier, quality, new UpgradeLevel(success.value() + 1), timeReduction);
            case TIME_REDUCTION -> new StationStats(tier, quality, success, new UpgradeLevel(timeReduction.value() + 1));
            default -> throw new IllegalStateException("Unexpected value: " + statType);
        };
    }
}
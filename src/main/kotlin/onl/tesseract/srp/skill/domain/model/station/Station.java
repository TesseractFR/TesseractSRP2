package onl.tesseract.srp.skill.domain.model.station;

import onl.tesseract.srp.domain.skill.station.StatType;
import onl.tesseract.srp.skill.domain.model.bonus.StationBonus;
import org.jetbrains.annotations.NotNull;

public record Station(
        @NotNull StationKey key,
        @NotNull StationStats stats
) {


    public int getStatLevel(@NotNull StatType tier) {
        return stats.getStatLevel(tier);
    }

    public StationBonus getBonus() {
        return stats.getBonus();
    }

    public Station upgradeStat(@NotNull StatType statType) {
        return new Station(key, stats.upgradeStat(statType));
    }
}

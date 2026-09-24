package onl.tesseract.srp.skill.domain.model.station;

import lombok.NonNull;
import onl.tesseract.srp.skill.domain.model.bonus.StationBonus;

public record Station(
        @NonNull StationKey key,
        @NonNull StationStats stats
) {


    public int getStatLevel(@NonNull StatType tier) {
        return stats.getStatLevel(tier);
    }

    public StationBonus getBonus() {
        return stats.getBonus();
    }

    public Station upgradeStat(@NonNull StatType statType) {
        return new Station(key, stats.upgradeStat(statType));
    }
}

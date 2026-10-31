package onl.tesseract.srp.customItem.adapter.serverside;

import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.srp.common.domain.model.enums.Quality;

import java.util.Arrays;

enum QualityAdapter {
    POOR(Quality.POOR, "Médiocre", NamedTextColor.RED),
    NORMAL(Quality.NORMAL, "Normale", NamedTextColor.WHITE),
    GOOD(Quality.GOOD, "Bonne", NamedTextColor.GREEN),
    VERY_GOOD(Quality.VERY_GOOD, "Très bonne", NamedTextColor.AQUA),
    EXCEPTIONAL(Quality.EXCEPTIONAL, "Exceptionnelle", NamedTextColor.GOLD);

    private final Quality quality;
    private final String label;
    private final NamedTextColor color;

    QualityAdapter(Quality quality, String label, NamedTextColor color) {
        this.quality = quality;
        this.label = label;
        this.color = color;
    }

    static QualityAdapter from(Quality quality) {
        return Arrays.stream(QualityAdapter.values())
                .filter(adapter -> adapter.quality == quality)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported quality: " + quality));
    }

    String label() {
        return label;
    }

    NamedTextColor color() {
        return color;
    }
}

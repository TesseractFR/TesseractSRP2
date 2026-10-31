package onl.tesseract.srp.customItem.adapter.serverside;

import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.srp.customitem.domain.model.Rarity;

import java.util.Arrays;

enum RarityAdapter {
    COMMON(Rarity.COMMON, "Commune", NamedTextColor.WHITE),
    SPECIAL(Rarity.SPECIAL, "Spéciale", NamedTextColor.GREEN),
    RARE(Rarity.RARE, "Rare", NamedTextColor.BLUE),
    EPIC(Rarity.EPIC, "Épique", NamedTextColor.DARK_PURPLE),
    LEGENDARY(Rarity.LEGENDARY, "Légendaire", NamedTextColor.GOLD);

    private final Rarity rarity;
    private final String label;
    private final NamedTextColor color;

    RarityAdapter(Rarity rarity, String label, NamedTextColor color) {
        this.rarity = rarity;
        this.label = label;
        this.color = color;
    }

    static RarityAdapter from(Rarity rarity) {
        return Arrays.stream(RarityAdapter.values())
                .filter(adapter -> adapter.rarity == rarity)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported rarity: " + rarity));

    }

    String label() {
        return label;
    }

    NamedTextColor color() {
        return color;
    }
}

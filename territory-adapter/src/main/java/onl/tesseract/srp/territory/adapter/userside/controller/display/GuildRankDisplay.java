package onl.tesseract.srp.territory.adapter.userside.controller.display;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildRank;

@Getter
public enum GuildRankDisplay {
    HAMEAU("Hameau", NamedTextColor.GRAY),
    COMMUNE("Commune", NamedTextColor.GREEN),
    VILLAGE("Village", NamedTextColor.YELLOW),
    VILLE("Ville", NamedTextColor.GOLD),
    CITE("Cité", NamedTextColor.AQUA),
    CAPITALE("Capitale", NamedTextColor.LIGHT_PURPLE);

    private final String title;
    private final NamedTextColor color;

    GuildRankDisplay(String title, NamedTextColor color) {
        this.title = title;
        this.color = color;
    }

    public static GuildRankDisplay of(GuildRank rank) {
        return switch (rank) {
            case HAMEAU -> HAMEAU;
            case COMMUNE -> COMMUNE;
            case VILLAGE -> VILLAGE;
            case VILLE -> VILLE;
            case CITE -> CITE;
            case CAPITALE -> CAPITALE;
        };
    }
}
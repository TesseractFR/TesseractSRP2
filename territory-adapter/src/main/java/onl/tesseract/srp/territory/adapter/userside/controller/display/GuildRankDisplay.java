package onl.tesseract.srp.territory.adapter.userside.controller.display;

import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildRank;

public final class GuildRankDisplay {
    private GuildRankDisplay() {}

    public static String title(GuildRank rank) {
        return switch (rank) {
            case HAMEAU -> "Hameau";
            case COMMUNE -> "Commune";
            case VILLAGE -> "Village";
            case VILLE -> "Ville";
            case CITE -> "Cité";
            case CAPITALE -> "Capitale";
        };
    }

    public static NamedTextColor color(GuildRank rank) {
        return switch (rank) {
            case HAMEAU -> NamedTextColor.GRAY;
            case COMMUNE -> NamedTextColor.GREEN;
            case VILLAGE -> NamedTextColor.YELLOW;
            case VILLE -> NamedTextColor.GOLD;
            case CITE -> NamedTextColor.AQUA;
            case CAPITALE -> NamedTextColor.LIGHT_PURPLE;
        };
    }
}
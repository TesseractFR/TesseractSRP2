package onl.tesseract.srp.territory.domain.model.guild.enums;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public enum GuildRank {
    HAMEAU("Hameau",1,0,9,5, NamedTextColor.GRAY),
    COMMUNE("Commune",2,5_000,15,10, NamedTextColor.GREEN),
    VILLAGE("Village",4,15_000,25,20, NamedTextColor.YELLOW),
    VILLE("Ville",7,50_000,40,35, NamedTextColor.GOLD),
    CITE("Cité",10,150_000,60,50, NamedTextColor.AQUA),
    CAPITALE("Capitale",14,400_000,100,100, NamedTextColor.LIGHT_PURPLE);

    private final String title;
    private final int minLevel;
    private final int cost;
    private final int maxChunksNumber;
    private final int maxMembersNumber;
    private final NamedTextColor color;

    GuildRank(String title, int minLevel, int cost, int maxChunksNumber, int maxMembersNumber, NamedTextColor color) {
        this.title = title;
        this.minLevel = minLevel;
        this.cost = cost;
        this.maxChunksNumber = maxChunksNumber;
        this.maxMembersNumber = maxMembersNumber;
        this.color = color;
    }


    public GuildRank next() {
        int nextOrdinal = ordinal() + 1;
        if (nextOrdinal >= values().length) return null;
        return values()[nextOrdinal];
    }
}


package onl.tesseract.srp.territory.domain.model.guild.enums;

import lombok.Getter;

@Getter
public enum GuildRank {
    HAMEAU(1, 0, 9, 5),
    COMMUNE(2, 5_000, 15, 10),
    VILLAGE(4, 15_000, 25, 20),
    VILLE(7, 50_000, 40, 35),
    CITE(10, 150_000, 60, 50),
    CAPITALE(14, 400_000, 100, 100);

    private final int minLevel;
    private final int cost;
    private final int maxChunksNumber;
    private final int maxMembersNumber;

    GuildRank(int minLevel, int cost, int maxChunksNumber, int maxMembersNumber) {
        this.minLevel = minLevel;
        this.cost = cost;
        this.maxChunksNumber = maxChunksNumber;
        this.maxMembersNumber = maxMembersNumber;
    }

    public GuildRank next() {
        int nextOrdinal = ordinal() + 1;
        return nextOrdinal >= values().length ? null : values()[nextOrdinal];
    }
}
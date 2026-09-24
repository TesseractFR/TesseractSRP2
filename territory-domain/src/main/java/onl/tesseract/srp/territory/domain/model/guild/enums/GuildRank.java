package onl.tesseract.srp.territory.domain.model.guild.enums;

public enum GuildRank {
    HAMEAU("Hameau", 1, 0),
    COMMUNE("Commune", 2, 5_000),
    VILLAGE("Village", 4, 15_000),
    VILLE("Ville", 7, 50_000),
    CITE("Cité", 10, 150_000),
    CAPITALE("Capitale", 14, 400_000);

    private final String title;
    private final int minLevel;
    private final int cost;

    GuildRank(String title, int minLevel, int cost) {
        this.title = title;
        this.minLevel = minLevel;
        this.cost = cost;
    }

    public String getTitle() {
        return title;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public int getCost() {
        return cost;
    }

    public GuildRank next() {
        int nextOrdinal = ordinal() + 1;
        if (nextOrdinal >= values().length) return null;
        return values()[nextOrdinal];
    }
}


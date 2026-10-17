package onl.tesseract.srp.territory.domain.model.guild.enums;

public enum GuildRank {
    HAMEAU("Hameau",1,0,9,5),
    COMMUNE("Commune",2,5_000,15,10),
    VILLAGE("Village",4,15_000,25,20),
    VILLE("Ville",7,50_000,40,35),
    CITE("Cité",10,150_000,60,50),
    CAPITALE("Capitale",14,400_000,100,100);

    private final String title;
    private final int minLevel;
    private final int cost;
    private final int maxChunksNumber;
    private final int maxMembersNumber;

    GuildRank(String title, int minLevel, int cost, int maxChunksNumber, int maxMembersNumber) {
        this.title = title;
        this.minLevel = minLevel;
        this.cost = cost;
        this.maxChunksNumber = maxChunksNumber;
        this.maxMembersNumber = maxMembersNumber;
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

    public int getMaxChunksNumber() { return maxChunksNumber; }
    public int getMaxMembersNumber() { return maxMembersNumber; }

    public GuildRank next() {
        int nextOrdinal = ordinal() + 1;
        if (nextOrdinal >= values().length) return null;
        return values()[nextOrdinal];
    }
}


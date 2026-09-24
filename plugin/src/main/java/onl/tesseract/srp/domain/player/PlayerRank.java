package onl.tesseract.srp.domain.player;

import onl.tesseract.core.title.Title;
import org.bukkit.Material;

public enum PlayerRank {
    Survivant(
        new Title("Survivant", "Survivant", "Survivante"),
        0, Material.WOODEN_SWORD, 90, 1, 1
    ),
    Explorateur(
        new Title("Explorateur", "Explorateur", "Exploratrice"),
        500, Material.STONE_SWORD, 90, 2, 2
    ),
    Aventurier(
        new Title("Aventurier", "Aventurier", "Aventurière"),
        2000, Material.IRON_SWORD, 80, 2, 3
    ),
    Noble(
        new Title("Noble", "Noble", "Noble"),
        10000, Material.GOLDEN_SWORD, 80, 3, 4
    ),
    Baron(
        new Title("Baron", "Baron", "Baronne"),
        50000, Material.DIAMOND_SWORD, 70, 3, 5
    ),
    Seigneur(
        new Title("Seigneur", "Seigneur", "Seigneur"),
        250000, Material.NETHERITE_SWORD, 70, 3, 6
    ),
    Vicomte(
        new Title("Vicomte", "Vicomte", "Vicomtesse"),
        1000000, Material.NETHERITE_SWORD, 60, 4, 7
    ),
    Comte(
        new Title("Comte", "Comte", "Comtesse"),
        5000000, Material.NETHERITE_SWORD, 60, 4, 8
    ),
    Duc(
        new Title("Duc", "Duc", "Duchesse"),
        15000000, Material.NETHERITE_SWORD, 50, 4, 9
    ),
    Roi(
        new Title("Roi", "Roi", "Reine"),
        50000000, Material.NETHERITE_SWORD, 50, 5, 10
    ),
    Empereur(
        new Title("Empereur", "Empereur", "Impératrice"),
        100000000, Material.NETHERITE_SWORD, 40, 5, 11
    );

    private final Title title;
    private final int cost;
    private final Material icon;
    private final int tpDelay;
    private final int jobMissionSlots;
    private final int campLevel;

    PlayerRank(Title title, int cost, Material icon, int tpDelay, int jobMissionSlots, int campLevel) {
        this.title = title;
        this.cost = cost;
        this.icon = icon;
        this.tpDelay = tpDelay;
        this.jobMissionSlots = jobMissionSlots;
        this.campLevel = campLevel;
    }

    public Title getTitle() { return title; }
    public int getCost() { return cost; }
    public Material getIcon() { return icon; }
    public int getTpDelay() { return tpDelay; }
    public int getJobMissionSlots() { return jobMissionSlots; }
    public int getCampLevel() { return campLevel; }

    public PlayerRank next() {
        if (this == Empereur) return null;
        return values()[ordinal() + 1];
    }

    public static PlayerRank getRequiredRankForMissionSlot(int slot) {
        for (PlayerRank rank : values()) {
            if (rank.jobMissionSlots >= slot) {
                return rank;
            }
        }
        throw new IllegalArgumentException("Invalid mission slot " + slot);
    }
}


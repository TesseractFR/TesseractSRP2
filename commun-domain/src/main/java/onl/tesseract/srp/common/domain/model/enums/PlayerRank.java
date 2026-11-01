package onl.tesseract.srp.common.domain.model.enums;

import lombok.Getter;
import onl.tesseract.core.title.Title;
public enum PlayerRank {
    Survivant(
            new Title("Survivant", "Survivant", "Survivante"),
            0, 90, 1
    ),
    Explorateur(
            new Title("Explorateur", "Explorateur", "Exploratrice"),
            500, 90, 2
    ),
    Aventurier(
            new Title("Aventurier", "Aventurier", "Aventurière"),
            2000, 80, 3
    ),
    Noble(
            new Title("Noble", "Noble", "Noble"),
            10000, 80, 4
    ),
    Baron(
            new Title("Baron", "Baron", "Baronne"),
            50000, 70, 5
    ),
    Seigneur(
            new Title("Seigneur", "Seigneur", "Seigneur"),
            250000, 70, 6
    ),
    Vicomte(
            new Title("Vicomte", "Vicomte", "Vicomtesse"),
            1000000, 60, 7
    ),
    Comte(
            new Title("Comte", "Comte", "Comtesse"),
            5000000, 60, 8
    ),
    Duc(
            new Title("Duc", "Duc", "Duchesse"),
            15000000, 50, 9
    ),
    Roi(
            new Title("Roi", "Roi", "Reine"),
            50000000, 50, 10
    ),
    Empereur(
            new Title("Empereur", "Empereur", "Impératrice"),
            100000000, 40, 11
    );


    @Getter
    private final Title title;
    @Getter
    private final int cost;
    @Getter
    private final int tpDelay;
    @Getter
    private final int campLevel;

    PlayerRank(Title title, int cost, int tpDelay, int campLevel) {
        this.title = title;
        this.cost = cost;
        this.tpDelay = tpDelay;
        this.campLevel = campLevel;
    }

    public PlayerRank next() {
        if (this == Empereur) return null;
        return values()[ordinal() + 1];
    }

}
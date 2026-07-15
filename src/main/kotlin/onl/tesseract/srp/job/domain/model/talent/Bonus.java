package onl.tesseract.srp.job.domain.model.talent;

public enum Bonus {
    QUALITY("Augmente la chance de qualité"),
    LOOT_CHANCE("Augmente la chance de butin"),
    MONEY("Augmente la quantité d'argent gagnée");

    private final String description;

    Bonus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

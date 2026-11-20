package onl.tesseract.srp.commission.domain.model.board;

/**
 * Palier d'une amélioration.
 *
 * @param cost       prix en lys, payé par la banque de la guilde
 * @param guildLevel niveau de guilde minimal
 */
public record UpgradeStep(int cost, int guildLevel) {
    /** Vérifie que le prix et le niveau ne sont pas négatifs. */
    public UpgradeStep {
        if (cost < 0) {
            throw new IllegalArgumentException("Le prix d'une amélioration ne peut pas être négatif : " + cost);
        }
        if (guildLevel < 0) {
            throw new IllegalArgumentException("Le niveau de guilde requis ne peut pas être négatif : " + guildLevel);
        }
    }
}

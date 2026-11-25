package onl.tesseract.srp.commission.adapter.serverside.yaml;

/**
 * Quantité demandée par commission pour les items d'une rareté.
 *
 * @param quantity        quantité de base, au moins un
 * @param variancePercent écart maximal autour de la base, en pourcentage, positif ou nul
 */
public record CommissionRarityQuantity(int quantity, double variancePercent) {

    /** Valeur utilisée pour une rareté absente de la configuration. */
    public static final CommissionRarityQuantity DEFAULT = new CommissionRarityQuantity(300, 0);

    public CommissionRarityQuantity {
        if (quantity < 1) {
            throw new IllegalArgumentException("La quantité d'une rareté vaut au moins 1 : " + quantity);
        }
        if (variancePercent < 0) {
            throw new IllegalArgumentException("La variance d'une rareté ne peut pas être négative : " + variancePercent);
        }
    }
}

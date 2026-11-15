package onl.tesseract.srp.contract.adapter.serverside.yaml;

/**
 * Quantité demandée par contrat pour les matériaux d'une rareté.
 *
 * @param quantity        quantité de base, strictement positive
 * @param variancePercent écart maximal autour de la base, en pourcentage, positif ou nul
 */
public record RarityQuantity(double quantity, double variancePercent) {

    /** Valeur utilisée pour une rareté absente de la configuration. */
    public static final RarityQuantity DEFAULT = new RarityQuantity(300, 0);

    /** Vérifie que la quantité est strictement positive et la variance positive ou nulle. */
    public RarityQuantity {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La quantité d'une rareté doit être strictement positive : " + quantity);
        }
        if (variancePercent < 0) {
            throw new IllegalArgumentException("La variance d'une rareté ne peut pas être négative : " + variancePercent);
        }
    }
}

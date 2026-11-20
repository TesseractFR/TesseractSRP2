package onl.tesseract.srp.commission.adapter.serverside.yaml;

/**
 * Quantité demandée par commission pour les items d'une rareté. Les valeurs sont vérifiées par
 * l'item du catalogue construit à partir d'elles.
 *
 * @param quantity        quantité de base
 * @param variancePercent écart maximal autour de la base, en pourcentage
 */
public record RarityQuantity(int quantity, double variancePercent) {
}

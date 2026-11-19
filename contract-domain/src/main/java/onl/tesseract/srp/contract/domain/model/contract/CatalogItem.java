package onl.tesseract.srp.contract.domain.model.contract;

import lombok.NonNull;
import onl.tesseract.srp.common.domain.model.item.MaterialName;

/**
 * Élément du catalogue utilisé pour générer des contrats selon une pondération.
 *
 * @param quantity                quantité de base demandée
 * @param quantityVariancePercent écart maximal de la quantité autour de la base, en pourcentage
 */
public record CatalogItem(@NonNull ContractType type, @NonNull ContractClient client, @NonNull MaterialName material,
                          double weight, int quantity, double quantityVariancePercent) {
    /** Vérifie que le poids est strictement positif, la quantité au moins égale à un et la variance positive ou nulle. */
    public CatalogItem {
        if (weight <= 0) {
            throw new IllegalArgumentException("Le poids d'un élément du catalogue doit être strictement positif : " + weight);
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("La quantité d'un élément du catalogue doit être au moins 1 : " + quantity);
        }
        if (quantityVariancePercent < 0) {
            throw new IllegalArgumentException("La variance de quantité ne peut pas être négative : " + quantityVariancePercent);
        }
    }
}

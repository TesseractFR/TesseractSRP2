package onl.tesseract.srp.commission.domain.model.commission;

import lombok.NonNull;
import onl.tesseract.srp.common.domain.model.item.MaterialName;

/**
 * Item qu'une commission peut demander.
 *
 * @param tier                    tier de la recette pour un item fabriqué, tier fixé en configuration pour un item récolté
 * @param weight                  poids de tirage, qui dépend de la rareté ; un poids nul exclut l'item
 * @param quantity                quantité de base demandée, qui dépend de la rareté
 * @param quantityVariancePercent écart maximal de la quantité autour de la base, en pourcentage, qui dépend de la rareté
 */
public record CatalogItem(@NonNull MaterialName material, int tier, double weight, int quantity,
                          double quantityVariancePercent) {
    /** Vérifie que le tier et la quantité valent au moins un, et que le poids et la variance ne sont pas négatifs. */
    public CatalogItem {
        if (tier < 1) {
            throw new IllegalArgumentException("Le tier d'un item du catalogue vaut au moins 1 : " + tier);
        }
        if (weight < 0) {
            throw new IllegalArgumentException("Le poids d'un item du catalogue ne peut pas être négatif : " + weight);
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("La quantité d'un item du catalogue vaut au moins 1 : " + quantity);
        }
        if (quantityVariancePercent < 0) {
            throw new IllegalArgumentException("La variance de quantité ne peut pas être négative : " + quantityVariancePercent);
        }
    }
}

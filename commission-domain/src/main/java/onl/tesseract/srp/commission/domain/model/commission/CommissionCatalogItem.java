package onl.tesseract.srp.commission.domain.model.commission;

import onl.tesseract.srp.common.domain.model.item.MaterialName;

/**
 * Item qu'une commission peut demander.
 *
 * @param tier                    tier de la recette pour un item fabriqué, tier fixé en configuration pour un item récolté
 * @param weight                  poids de tirage, qui dépend de la rareté ; un poids nul exclut l'item
 * @param quantity                quantité de base demandée, qui dépend de la rareté
 * @param quantityVariancePercent écart maximal de la quantité autour de la base, en pourcentage, qui dépend de la rareté
 */
public record CommissionCatalogItem(MaterialName material, int tier, double weight, int quantity,
                          double quantityVariancePercent) {
}

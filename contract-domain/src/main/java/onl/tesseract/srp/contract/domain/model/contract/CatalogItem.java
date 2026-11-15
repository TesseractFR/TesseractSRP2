package onl.tesseract.srp.contract.domain.model.contract;

import lombok.NonNull;
import onl.tesseract.srp.common.domain.model.item.MaterialName;

/** Élément du catalogue utilisé pour générer des contrats selon une pondération. */
public record CatalogItem(@NonNull ContractType type, @NonNull ContractClient client, @NonNull MaterialName material,
                          double weight, int quantity) {
    /** Vérifie que le poids est strictement positif et la quantité au moins égale à un. */
    public CatalogItem {
        if (weight <= 0) {
            throw new IllegalArgumentException("Le poids d'un élément du catalogue doit être strictement positif : " + weight);
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("La quantité d'un élément du catalogue doit être au moins 1 : " + quantity);
        }
    }
}

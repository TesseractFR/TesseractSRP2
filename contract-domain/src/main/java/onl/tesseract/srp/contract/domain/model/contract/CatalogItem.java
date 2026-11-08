package onl.tesseract.srp.contract.domain.model.contract;

import lombok.NonNull;

/** Élément du catalogue utilisé pour générer des contrats selon une pondération. */
public record CatalogItem(@NonNull ContractType type, @NonNull String job, @NonNull String material, double weight,
                          int quantity) {
    /** Vérifie que le poids est strictement positif et la quantité au moins égale à un. */
    public CatalogItem {
        if (weight <= 0) throw new IllegalArgumentException("weight must be > 0");
        if (quantity < 1) throw new IllegalArgumentException("quantity must be >= 1");
    }
}

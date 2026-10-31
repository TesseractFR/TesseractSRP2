package onl.tesseract.srp.contract.domain.model.contract;

import lombok.NonNull;

public record CatalogItem(@NonNull ContractType type, @NonNull String job, @NonNull String material, double weight,
                          int quantity) {
    public CatalogItem {
        if (weight <= 0) throw new IllegalArgumentException("weight must be > 0");
        if (quantity < 1) throw new IllegalArgumentException("quantity must be >= 1");
    }
}
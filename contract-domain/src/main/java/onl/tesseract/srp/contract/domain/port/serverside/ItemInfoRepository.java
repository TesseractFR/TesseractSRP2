package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.contract.domain.model.Quality;
import onl.tesseract.srp.contract.domain.model.contract.ContractType;

import java.util.List;
import java.util.Objects;

public interface ItemInfoRepository {
    List<CatalogItem> getCatalogItems();
    double getValue(String material, Quality quality);

    record CatalogItem(ContractType type, String job, String material, double weight, int quantity) {
        public CatalogItem {
            Objects.requireNonNull(type, "type cannot be null");
            Objects.requireNonNull(job, "job cannot be null");
            Objects.requireNonNull(material, "material cannot be null");
            if (weight <= 0) throw new IllegalArgumentException("weight must be > 0");
            if (quantity < 1) throw new IllegalArgumentException("quantity must be >= 1");
        }
    }
}
package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;
import onl.tesseract.srp.contract.domain.model.contract.CatalogItem;

import java.util.List;

/** Port d'accès au catalogue des contrats et à la valeur des matériaux. */
public interface ContractCatalogRepository {
    /** @return éléments disponibles pour la génération de contrats, liste vide si aucun */
    List<CatalogItem> findAllCatalogItems();

    /** @return valeur d'une unité du matériau à la qualité indiquée */
    MoneyValue getUnitValue(MaterialName material, Quality quality);
}

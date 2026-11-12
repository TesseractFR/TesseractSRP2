package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;
import onl.tesseract.srp.contract.domain.model.contract.CatalogItem;

import java.util.List;

/** Port d'accès au catalogue des contrats et à la valeur des matériaux. */
public interface ItemInfoRepository {
    /** @return éléments disponibles pour la génération de contrats */
    List<CatalogItem> getCatalogItems();
    /** Retourne la valeur du matériau pour la qualité indiquée. */
    double getValue(MaterialName material, Quality quality);
}

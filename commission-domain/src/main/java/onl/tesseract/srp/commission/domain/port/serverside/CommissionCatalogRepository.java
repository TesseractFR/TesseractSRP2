package onl.tesseract.srp.commission.domain.port.serverside;

import onl.tesseract.srp.commission.domain.model.commission.CommissionCatalogItem;
import onl.tesseract.srp.commission.domain.model.commission.CommissionType;
import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;

import java.util.List;
import java.util.Optional;

/** Port d'accès au catalogue des items que les commissions peuvent demander, et à leur valeur. */
public interface CommissionCatalogRepository {
    /** @return items que ce type de commission peut demander, tous avec un prix ; liste vide si aucun */
    List<CommissionCatalogItem> findAllCatalogItems(CommissionType commissionType);

    /** @return valeur d'une unité de l'item à la qualité indiquée */
    MoneyValue getUnitValue(MaterialName material, Quality quality);

    /**
     * @return tier de l'item pour ce type, ou {@link Optional#empty()} s'il n'est pas dans le catalogue :
     * une commission en cours garde un item retiré de la configuration ou des recettes
     */
    Optional<Integer> findItemTier(CommissionType commissionType, MaterialName material);
}

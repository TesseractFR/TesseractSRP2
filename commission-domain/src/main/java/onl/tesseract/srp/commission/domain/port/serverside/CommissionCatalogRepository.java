package onl.tesseract.srp.commission.domain.port.serverside;

import onl.tesseract.srp.commission.domain.model.commission.CommissionCatalogItem;
import onl.tesseract.srp.commission.domain.model.commission.CommissionType;
import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;

import java.util.List;

/** Port d'accès au catalogue des items que les commissions peuvent demander, et à leur valeur. */
public interface CommissionCatalogRepository {
    /** @return items que ce type de commission peut demander, tous avec un prix ; liste vide si aucun */
    List<CommissionCatalogItem> findAllCatalogItems(CommissionType commissionType);

    /** @return valeur d'une unité de l'item à la qualité indiquée */
    MoneyValue getUnitValue(MaterialName material, Quality quality);

    /** @return tier de l'item pour ce type ; un item demandé par une commission est toujours dans le catalogue */
    int getItemTier(CommissionType commissionType, MaterialName material);
}

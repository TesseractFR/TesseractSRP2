package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.contract.domain.model.Quality;
import onl.tesseract.srp.contract.domain.model.contract.CatalogItem;

import java.util.List;

public interface ItemInfoRepository {
    List<CatalogItem> getCatalogItems();
    double getValue(String material, Quality quality);
}
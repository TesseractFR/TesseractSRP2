package onl.tesseract.srp.customitem.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.customitem.domain.model.MaterialName;

public interface MoneyRepository {
    MoneyValue getValue(MaterialName materialName, Quality quality);
}

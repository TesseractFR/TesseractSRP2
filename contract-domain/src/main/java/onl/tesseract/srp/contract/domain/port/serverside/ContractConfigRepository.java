package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.contract.domain.model.settings.ContractSettings;

public interface ContractConfigRepository {

    ContractSettings current();
}

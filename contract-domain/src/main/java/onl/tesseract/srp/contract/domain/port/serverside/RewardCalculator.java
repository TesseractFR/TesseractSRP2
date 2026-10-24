package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.contract.domain.model.contract.Contract;

public interface RewardCalculator {
    int reward(Contract draft);
}

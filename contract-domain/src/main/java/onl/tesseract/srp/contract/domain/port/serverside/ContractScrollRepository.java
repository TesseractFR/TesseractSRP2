package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.domain.model.contract.Contract;

public interface ContractScrollRepository {
    boolean canReceive(PlayerId player);
    boolean give(PlayerId player, Contract contract, boolean hasJobBonus);
    boolean has(PlayerId player, long contractId);
    boolean remove(PlayerId player, long contractId);
}
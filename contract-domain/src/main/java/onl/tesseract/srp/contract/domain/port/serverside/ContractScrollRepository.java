package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.model.contract.Contract;

public interface ContractScrollRepository {
    boolean canReceive(PlayerID player);
    boolean give(PlayerID player, Contract contract, boolean hasJobBonus);
    boolean has(PlayerID player, long contractId);
    boolean remove(PlayerID player, long contractId);
}
package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.model.ticket.ContractPlayerState;

public interface ContractPlayerStateRepository {

    ContractPlayerState find(PlayerID player);

    void save(ContractPlayerState state);
}

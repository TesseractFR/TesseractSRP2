package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.model.profile.PlayerContractProfile;

public interface PlayerProfileRepository {
    PlayerContractProfile profileOf(PlayerID player);
    int dailyTickets(PlayerID player);
}

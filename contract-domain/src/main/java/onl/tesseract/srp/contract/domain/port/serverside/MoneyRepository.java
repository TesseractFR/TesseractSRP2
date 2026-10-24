package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.contract.domain.model.PlayerID;

public interface MoneyRepository {
    boolean pay(PlayerID player, int amount, String reason);
}

package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.model.Quality;
import onl.tesseract.srp.contract.domain.model.contract.Contract;

import java.util.Map;

public interface PlayerInventoryRepository {
    int countAtLeast(PlayerID player, String material, Quality minimalQuality);
    Map<Quality, Integer> removeAtLeast(PlayerID player, String material, Quality minimalQuality, int amount);
    void giveBack(PlayerID player, String material, Map<Quality, Integer> items);

    boolean canReceiveScroll(PlayerID player);
    boolean giveScroll(PlayerID player, Contract contract);
    boolean hasScroll(PlayerID player, long contractId);
    boolean removeScroll(PlayerID player, long contractId);
}
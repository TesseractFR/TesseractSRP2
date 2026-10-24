package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.model.Quality;

import java.util.Map;

public interface ItemRepository {
    int countAtLeast(PlayerID player, String material, Quality min);
    Map<Quality, Integer> removeAtLeast(PlayerID player, String material, Quality min, int amount);
    void giveBack(PlayerID player, String material, Map<Quality, Integer> items);
}

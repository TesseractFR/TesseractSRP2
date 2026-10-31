package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.model.Quality;

import java.util.Map;

public interface PlayerMaterialRepository {
    int countAtLeast(PlayerID player, String material, Quality minimalQuality);
    Map<Quality, Integer> removeAtLeast(PlayerID player, String material, Quality minimalQuality, int amount);
    void giveBack(PlayerID player, String material, Map<Quality, Integer> items);
}
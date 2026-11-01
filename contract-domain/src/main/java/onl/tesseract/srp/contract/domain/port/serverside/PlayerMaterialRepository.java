package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.enums.Quality;

import java.util.Map;

public interface PlayerMaterialRepository {
    int countAtLeast(PlayerId player, String material, Quality minimalQuality);
    Map<Quality, Integer> removeAtLeast(PlayerId player, String material, Quality minimalQuality, int amount);
    void giveBack(PlayerId player, String material, Map<Quality, Integer> items);
}
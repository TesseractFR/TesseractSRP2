package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.enums.Quality;

import java.util.Map;

/** Port de consultation, de retrait et de restitution des matériaux des joueurs. */
public interface PlayerMaterialRepository {
    /** Compte les unités du matériau dont la qualité est au moins celle demandée. */
    int countAtLeast(PlayerId player, String material, Quality minimalQuality);
    /** Retire la quantité demandée en respectant la qualité minimale et retourne le détail par qualité. */
    Map<Quality, Integer> removeAtLeast(PlayerId player, String material, Quality minimalQuality, int amount);
    /** Restitue au joueur les unités détaillées par qualité. */
    void giveBack(PlayerId player, String material, Map<Quality, Integer> items);
}

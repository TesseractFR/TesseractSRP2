package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;

/** Port de consultation, de retrait et de restitution des matériaux des joueurs. */
public interface PlayerMaterialRepository {
    /** Compte les unités du matériau dont la qualité est au moins celle demandée. */
    int countAtLeast(PlayerId player, MaterialName material, Quality minimalQuality);
    /**
     * Retire la quantité demandée en respectant la qualité minimale et retourne le détail par qualité.
     */
    void removeAtLeast(PlayerId player, MaterialName material, Quality minimalQuality, int amount);
}

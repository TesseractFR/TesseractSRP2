package onl.tesseract.srp.commission.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;

/** Port de consultation et de retrait des matériaux des joueurs. */
public interface PlayerMaterialPort {
    /** @return nombre d'unités du matériau dont la qualité est au moins celle demandée */
    int countAtLeast(PlayerId player, MaterialName material, Quality minimalQuality);

    /**
     * Retire la quantité demandée parmi les unités dont la qualité est au moins celle demandée,
     * en commençant par les qualités les plus basses. L'appelant vérifie au préalable
     * que le joueur possède cette quantité.
     */
    void removeAtLeast(PlayerId player, MaterialName material, Quality minimalQuality, int quantity);
}

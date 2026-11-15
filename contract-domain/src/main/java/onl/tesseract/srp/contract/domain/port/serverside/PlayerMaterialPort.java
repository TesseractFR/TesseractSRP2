package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;
import onl.tesseract.srp.contract.domain.model.exception.NotEnoughMaterialException;

/** Port de consultation et de retrait des matériaux des joueurs. */
public interface PlayerMaterialPort {
    /** Compte les unités du matériau dont la qualité est au moins celle demandée. */
    int countAtLeast(PlayerId player, MaterialName material, Quality minimalQuality);

    /**
     * Retire la quantité demandée parmi les unités dont la qualité est au moins celle demandée,
     * en commençant par les qualités les plus basses.
     *
     * @throws NotEnoughMaterialException si le joueur possède moins que la quantité demandée
     */
    void removeAtLeast(PlayerId player, MaterialName material, Quality minimalQuality, int quantity);
}

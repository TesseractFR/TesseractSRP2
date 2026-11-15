package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.model.contract.ContractId;

/** Port de gestion des parchemins de contrat dans l'inventaire des joueurs. */
public interface ContractScrollPort {
    /**
     * @return {@code true} si le joueur est en ligne et peut recevoir un parchemin supplémentaire
     */
    boolean canReceiveScroll(PlayerId player);

    /**
     * Crée et remet au joueur le parchemin du contrat.
     *
     * @param hasJobBonus indique, pour la présentation du parchemin, si le contrat ouvre droit à un bonus de métier
     * @return {@code true} si le parchemin a été remis, {@code false} si le joueur est hors ligne ou son inventaire plein
     */
    boolean giveScroll(PlayerId player, Contract contract, boolean hasJobBonus);

    /** @return {@code true} si le joueur en ligne possède le parchemin du contrat */
    boolean hasScroll(PlayerId player, ContractId contractId);

    /**
     * Retire au joueur le parchemin du contrat.
     *
     * @return {@code true} si un parchemin a été trouvé et retiré
     */
    boolean removeScroll(PlayerId player, ContractId contractId);
}

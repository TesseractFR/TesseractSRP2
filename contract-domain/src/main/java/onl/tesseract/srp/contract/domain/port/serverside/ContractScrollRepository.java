package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.domain.model.contract.Contract;

/** Port de gestion des parchemins de contrat détenus par les joueurs. */
public interface ContractScrollRepository {
    /** Indique si le joueur peut recevoir un parchemin. */
    boolean canReceive(PlayerId player);
    /** Remet le parchemin du contrat au joueur, avec l'indication de bonus affichée. */
    boolean give(PlayerId player, Contract contract, boolean hasJobBonus);
    /** Vérifie si le joueur possède un parchemin lié au contrat indiqué. */
    boolean has(PlayerId player, long contractId);
    /** Retire le parchemin du contrat indiqué. */
    boolean remove(PlayerId player, long contractId);
}

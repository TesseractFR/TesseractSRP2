package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.domain.model.contract.Contract;

/** Port de gestion des parchemins de contrat détenus par les joueurs. */
public interface ContractScrollRepository {
    /**
     * Vérifie que le joueur peut recevoir un parchemin supplémentaire.
     *
     * @param player joueur destinataire
     * @return {@code true} si la remise peut être tentée sans dépasser les capacités disponibles
     */
    boolean canReceiveScroll(PlayerId player);
    /**
     * Crée et remet au joueur un parchemin associé au contrat.
     * L'indication de bonus est destinée à la présentation du parchemin.
     *
     * @param player joueur destinataire
     * @param contract contrat identifié par le parchemin
     * @param hasJobBonus indique si le contrat bénéficie d'un bonus lié au métier
     * @return {@code true} si le parchemin a été remis
     */
    boolean giveScroll(PlayerId player, Contract contract, boolean hasJobBonus);
    /**
     * Recherche dans les parchemins du joueur celui dont l'identifiant de contrat correspond.
     *
     * @param player joueur concerné
     * @param contractId identifiant du contrat recherché
     * @return {@code true} si le joueur possède un parchemin associé à cet identifiant
     */
    boolean hasScroll(PlayerId player, long contractId);
    /**
     * Retire du joueur le parchemin associé à l'identifiant de contrat donné.
     *
     * @param player joueur concerné
     * @param contractId identifiant du contrat associé au parchemin
     * @return {@code true} si un parchemin a été trouvé et retiré
     */
    boolean removeScroll(PlayerId player, long contractId);
}

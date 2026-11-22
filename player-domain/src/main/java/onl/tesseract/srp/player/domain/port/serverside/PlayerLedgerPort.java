package onl.tesseract.srp.player.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.player.domain.model.PlayerRank;

/** Journal des mouvements d'argent du joueur. */
public interface PlayerLedgerPort {

    /** Enregistre le paiement du grade acheté : son coût passe du compte du joueur à celui du serveur. */
    void recordRankPurchase(PlayerId playerId, PlayerRank purchasedRank);

    /**
     * Enregistre un ajustement de solde fait par le staff.
     *
     * @param amount positif si de l'argent est donné au joueur, négatif s'il lui est retiré
     */
    void recordStaffMoneyAdjustment(PlayerId playerId, int amount);
}

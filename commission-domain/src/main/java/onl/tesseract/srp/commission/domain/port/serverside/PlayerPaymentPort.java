package onl.tesseract.srp.commission.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.PlayerId;

/** Port de versement des gains de livraison aux joueurs. */
public interface PlayerPaymentPort {
    /** Verse le montant, en lys, au joueur. Un montant nul ne change rien. */
    void pay(PlayerId player, int amount);
}

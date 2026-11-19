package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.domain.model.exception.PaymentFailedException;

/** Port de versement d'argent aux joueurs. */
public interface PaymentPort {
    /**
     * Verse le montant au joueur.
     *
     * @throws PaymentFailedException si le versement est refusé
     */
    void pay(PlayerId player, MoneyValue amount);
}

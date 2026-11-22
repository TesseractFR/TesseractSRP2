package onl.tesseract.srp.contract.adapter.serverside.payment;

import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.player.domain.port.userside.PlayerMoneyService;
import onl.tesseract.srp.contract.domain.port.serverside.PaymentPort;
import org.springframework.stereotype.Component;

/** Verse l'argent des contrats au moyen de {@link PlayerMoneyService}. */
@Component
public class PlayerServicePaymentPort implements PaymentPort {

    private final PlayerMoneyService playerMoneyService;

    public PlayerServicePaymentPort(PlayerMoneyService playerMoneyService) {
        this.playerMoneyService = playerMoneyService;
    }

    /** Un montant de contrat n'est jamais négatif : l'ajout au solde du joueur ne peut pas être refusé. */
    @Override
    public void pay(PlayerId player, MoneyValue amount) {
        playerMoneyService.addMoney(player, amount.value().intValueExact());
    }
}

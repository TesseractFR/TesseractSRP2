package onl.tesseract.srp.contract.adapter.serverside.payment;

import lombok.extern.slf4j.Slf4j;
import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.port.userside.PlayerMoneyService;
import onl.tesseract.srp.contract.domain.model.exception.PaymentFailedException;
import onl.tesseract.srp.contract.domain.port.serverside.PaymentPort;
import org.springframework.stereotype.Component;

/** Verse l'argent des contrats au moyen de {@link PlayerMoneyService}. */
@Slf4j
@Component
public class PlayerServicePaymentPort implements PaymentPort {

    private final PlayerMoneyService playerMoneyService;

    public PlayerServicePaymentPort(PlayerMoneyService playerMoneyService) {
        this.playerMoneyService = playerMoneyService;
    }

    /**
     * Le versement a lieu après le retrait des objets du livreur : un échec est journalisé en erreur
     * pour que le staff puisse dédommager le joueur.
     */
    @Override
    public void pay(PlayerId player, MoneyValue amount) {
        if (!playerMoneyService.adjustMoney(player, amount.value().intValueExact())) {
            log.error("Échec du versement de {} lys au joueur {} pour une livraison de contrat", amount.value(), player.value());
            throw new PaymentFailedException(player, amount);
        }
    }
}

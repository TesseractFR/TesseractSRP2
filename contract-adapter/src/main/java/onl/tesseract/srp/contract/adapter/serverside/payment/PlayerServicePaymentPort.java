package onl.tesseract.srp.contract.adapter.serverside.payment;

import lombok.extern.slf4j.Slf4j;
import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.player.domain.port.userside.PlayerMoneyService;
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
     * Le versement a lieu après le retrait des objets du livreur : un montant refusé par le service joueur
     * (négatif, ou trop grand pour un {@code int}) est journalisé en erreur pour que le staff puisse dédommager
     * le joueur, puis traduit en {@link PaymentFailedException}.
     */
    @Override
    public void pay(PlayerId player, MoneyValue amount) {
        try {
            playerMoneyService.addMoney(player, amount.value().intValueExact());
        } catch (IllegalArgumentException | ArithmeticException exception) {
            log.error("Échec du versement de {} lys au joueur {} pour une livraison de contrat", amount.value(), player.value(), exception);
            throw new PaymentFailedException(player, amount);
        }
    }
}

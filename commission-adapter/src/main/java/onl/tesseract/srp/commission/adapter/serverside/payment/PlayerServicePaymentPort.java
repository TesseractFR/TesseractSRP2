package onl.tesseract.srp.commission.adapter.serverside.payment;

import onl.tesseract.srp.commission.domain.port.serverside.PlayerPaymentPort;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.player.domain.port.userside.PlayerMoneyService;
import org.springframework.stereotype.Component;

/**
 * Verse les gains de livraison au moyen de {@link PlayerMoneyService}. Le nom du bean est explicite
 * car le module contrat déclare une classe du même nom.
 */
@Component("commissionPlayerPaymentPort")
public class PlayerServicePaymentPort implements PlayerPaymentPort {

    private final PlayerMoneyService playerMoneyService;

    public PlayerServicePaymentPort(PlayerMoneyService playerMoneyService) {
        this.playerMoneyService = playerMoneyService;
    }

    @Override
    public void pay(PlayerId player, int amount) {
        playerMoneyService.addMoney(player, amount);
    }
}

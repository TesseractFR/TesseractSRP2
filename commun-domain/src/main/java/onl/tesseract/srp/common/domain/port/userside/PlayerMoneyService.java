package onl.tesseract.srp.common.domain.port.userside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.SrpPlayer;

import java.util.Objects;

/** Gestion du solde d'argent d'un joueur. */
public class PlayerMoneyService {

    private final SrpPlayerProfileService srpPlayerProfileService;

    public PlayerMoneyService(SrpPlayerProfileService srpPlayerProfileService) {
        this.srpPlayerProfileService = Objects.requireNonNull(srpPlayerProfileService);
    }

    /**
     * Ajoute (montant positif) ou retire (montant négatif) de l'argent au joueur.
     *
     * @return {@code false} si le solde deviendrait négatif, rien n'est alors modifié
     */
    public boolean adjustMoney(PlayerId playerId, int amount) {
        SrpPlayer player = srpPlayerProfileService.getPlayer(playerId);
        if ((long) player.getMoney() + amount < 0) return false;
        player.addMoney(amount);
        srpPlayerProfileService.savePlayer(player);
        return true;
    }
}

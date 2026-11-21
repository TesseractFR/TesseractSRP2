package onl.tesseract.srp.common.domain.port.userside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.SrpPlayer;

import java.util.Objects;

/** Gestion des points d'illumination d'un joueur. */
public class PlayerIlluminationService {

    private final SrpPlayerProfileService srpPlayerProfileService;

    public PlayerIlluminationService(SrpPlayerProfileService srpPlayerProfileService) {
        this.srpPlayerProfileService = Objects.requireNonNull(srpPlayerProfileService);
    }

    /**
     * Ajoute (montant positif) ou retire (montant négatif) des points d'illumination au joueur.
     *
     * @return {@code false} si le total deviendrait négatif, rien n'est alors modifié
     */
    public boolean adjustIlluminationPoints(PlayerId playerId, int amount) {
        SrpPlayer player = srpPlayerProfileService.getPlayer(playerId);
        if ((long) player.getIlluminationPoints() + amount < 0) return false;
        player.addIlluminationPoints(amount);
        srpPlayerProfileService.savePlayer(player);
        return true;
    }
}

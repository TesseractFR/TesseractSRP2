package onl.tesseract.srp.player.domain.port.userside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.player.domain.model.SrpPlayer;

import java.util.Objects;

/** Gestion des points d'illumination d'un joueur. */
public class PlayerIlluminationService {

    private final SrpPlayerProfileService srpPlayerProfileService;

    public PlayerIlluminationService(SrpPlayerProfileService srpPlayerProfileService) {
        this.srpPlayerProfileService = Objects.requireNonNull(srpPlayerProfileService);
    }

    /**
     * Ajoute des points d'illumination au joueur. Le dépassement de la valeur maximale n'est pas contrôlé.
     *
     * @throws IllegalArgumentException si le nombre de points est négatif
     */
    public void addIlluminationPoints(PlayerId playerId, int amount) {
        requireNotNegative(amount);
        adjustIlluminationPoints(playerId, amount);
    }

    /**
     * Retire des points d'illumination au joueur.
     *
     * @return {@code false} si le joueur n'a pas assez de points, rien n'est alors modifié
     * @throws IllegalArgumentException si le nombre de points est négatif
     */
    public boolean removeIlluminationPoints(PlayerId playerId, int amount) {
        requireNotNegative(amount);
        return adjustIlluminationPoints(playerId, -amount);
    }

    /** Applique la variation, sauf si elle ferait passer le total sous zéro. */
    private boolean adjustIlluminationPoints(PlayerId playerId, int variation) {
        SrpPlayer player = srpPlayerProfileService.getPlayer(playerId);
        if (variation < 0 && player.getIlluminationPoints() < -variation) return false;
        player.addIlluminationPoints(variation);
        srpPlayerProfileService.savePlayer(player);
        return true;
    }

    private static void requireNotNegative(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Le nombre de points ne peut pas être négatif : " + amount);
        }
    }
}

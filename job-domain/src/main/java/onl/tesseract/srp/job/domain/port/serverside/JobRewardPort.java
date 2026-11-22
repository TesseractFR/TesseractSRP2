package onl.tesseract.srp.job.domain.port.serverside;

import lombok.NonNull;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.job.domain.model.Material;
import onl.tesseract.srp.common.domain.model.PlayerId;

/**
 * Port de remise d'objets aux joueurs, utilisé pour les récompenses de métier.
 */
public interface JobRewardPort {
    /**
     * Ajoute des objets dans l'inventaire du joueur. Un matériau personnalisé est donné avec la qualité
     * demandée ; un matériau vanilla est donné sans qualité.
     *
     * @param playerId le joueur qui reçoit les objets
     * @param material le matériau à donner
     * @param quantity le nombre d'objets
     * @param quality  la qualité, utilisée seulement pour un matériau personnalisé
     */
    void giveItem(@NonNull PlayerId playerId, Material material, int quantity, Quality quality);

}

package onl.tesseract.srp.job.domain.port.serverside;

import onl.tesseract.srp.job.domain.model.JobName;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.job.domain.model.PlayerJobProgression;

/**
 * Port de lecture de la progression d'un joueur dans un métier (niveaux de talent et points).
 */
public interface JobPlayerProgressionRepository {
    /**
     * Lit la progression d'un joueur dans un métier.
     *
     * @param player  le joueur
     * @param jobName le métier
     * @return la progression enregistrée ; si le joueur n'en a pas, une progression vide
     *         (aucun talent, 0 point disponible, 0 point au total). Ne renvoie jamais {@code null}.
     */
    PlayerJobProgression getPlayerJobProgression(PlayerId player, JobName jobName);

}

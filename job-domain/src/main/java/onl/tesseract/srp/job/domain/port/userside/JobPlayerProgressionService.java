package onl.tesseract.srp.job.domain.port.userside;

import lombok.NonNull;
import onl.tesseract.srp.job.domain.model.JobName;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.job.domain.model.PlayerJobProgression;
import onl.tesseract.srp.job.domain.model.talent.Talent;
import onl.tesseract.srp.job.domain.model.talent.TalentName;
import onl.tesseract.srp.job.domain.port.serverside.JobPlayerProgressionRepository;

/**
 * Consulte la progression d'un joueur dans les talents de ses métiers : niveaux, coûts et disponibilité.
 */
public class JobPlayerProgressionService {
    private final JobPlayerProgressionRepository jobPlayerProgressionRepository;

    public JobPlayerProgressionService(JobPlayerProgressionRepository jobPlayerProgressionRepository) {
        this.jobPlayerProgressionRepository = jobPlayerProgressionRepository;
    }


    /**
     * Indique si un talent peut être débloqué : il n'a aucun parent, ou le joueur a au moins un niveau
     * dans chacun de ses parents.
     *
     * @param playerId le joueur
     * @param jobName  le métier
     * @param talent   le talent
     * @return {@code true} si le talent est accessible
     */
    public boolean isAvailable(PlayerId playerId,JobName jobName,Talent talent) {
        return getJobProgression(playerId, jobName).isTalentAvailable(talent);
    }

    /**
     * Donne le niveau du joueur dans un talent.
     *
     * @param playerId    le joueur
     * @param job         le métier
     * @param talentName  le talent
     * @return le niveau, 0 si le joueur ne l'a jamais amélioré
     */
    public int getTalentLevel(PlayerId playerId, JobName job, TalentName talentName) {
        return getJobProgression(playerId, job).getTalentLevel(talentName);
    }

    /**
     * Donne le prix en points de talent du prochain niveau d'un talent.
     *
     * @param playerId le joueur
     * @param job      le métier
     * @param talent   le talent
     * @return le prix du niveau suivant
     * @throws IllegalStateException si le talent est déjà à son niveau maximal
     */
    public int getTalentCost(PlayerId playerId, JobName job, Talent talent) {
        return talent.getPriceForLevel(getTalentLevel(playerId, job, talent.name()) + 1);
    }

    /**
     * Indique si le joueur a assez de points de talent disponibles pour acheter le prochain niveau.
     *
     * @param playerId le joueur
     * @param jobName  le métier
     * @param talent   le talent
     * @return {@code true} si les points disponibles couvrent le prix
     * @throws IllegalStateException si le talent est déjà à son niveau maximal
     */
    public boolean canBuyUpgrade(PlayerId playerId, JobName jobName, Talent talent){
        return getTalentCost(playerId, jobName, talent) <= getJobProgression(playerId, jobName).availableTalentPoint();
    }


    private PlayerJobProgression getJobProgression(PlayerId playerId, JobName jobName) {
        return jobPlayerProgressionRepository.getPlayerJobProgression(playerId, jobName);
    }

    /**
     * Améliore un talent du joueur.
     * <p>
     * Non implémenté : ne fait rien et renvoie toujours {@code false}.
     *
     * @param playerId le joueur
     * @param skill    le talent
     * @return toujours {@code false}
     */
    public boolean upgradeSkill(@NonNull PlayerId playerId, Talent skill) {
        return false;
    }
}

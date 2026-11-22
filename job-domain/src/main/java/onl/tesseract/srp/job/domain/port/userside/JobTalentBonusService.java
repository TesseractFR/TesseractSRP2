package onl.tesseract.srp.job.domain.port.userside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.job.domain.model.JobName;
import onl.tesseract.srp.job.domain.model.Material;
import onl.tesseract.srp.job.domain.model.talent.Bonus;

/** Calcule les bonus qu'un joueur tire des talents de ses métiers. */
public class JobTalentBonusService {

    private final JobCatalogService jobCatalogService;
    private final JobPlayerProgressionService jobPlayerProgressionService;

    public JobTalentBonusService(JobCatalogService jobCatalogService,
                                 JobPlayerProgressionService jobPlayerProgressionService) {
        this.jobCatalogService = jobCatalogService;
        this.jobPlayerProgressionService = jobPlayerProgressionService;
    }

    /**
     * Additionne la valeur des talents du métier qui portent un bonus donné sur un objet,
     * selon le niveau du joueur dans chacun de ces talents.
     *
     * @param jobName  le métier
     * @param playerId le joueur
     * @param item     l'objet concerné
     * @param bonus    le type de bonus
     * @return la somme des valeurs, 0.0 si le métier est inconnu
     */
    public double getBonusValue(JobName jobName, PlayerId playerId, Material item, Bonus bonus) {
        return jobCatalogService.findLoadedJob(jobName)
                .map(job -> job.talents().findAllByItemAndBonus(item, bonus).stream()
                        .mapToDouble(talent -> talent.getValue(
                                jobPlayerProgressionService.getTalentLevel(playerId, jobName, talent.name())))
                        .sum())
                .orElse(0.0);
    }
}

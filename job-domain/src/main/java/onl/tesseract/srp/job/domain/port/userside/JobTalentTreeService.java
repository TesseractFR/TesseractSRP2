package onl.tesseract.srp.job.domain.port.userside;

import onl.tesseract.srp.job.domain.model.JobName;
import onl.tesseract.srp.job.domain.model.talenttree.TalentTree;
import onl.tesseract.srp.job.domain.port.serverside.JobSkillTreeRepository;

/**
 * Donne accès à l'arbre de talents des métiers.
 */
public class JobTalentTreeService {

    private final JobSkillTreeRepository jobSkillTreeRepository;

    public JobTalentTreeService(JobSkillTreeRepository jobSkillTreeRepository) {
        this.jobSkillTreeRepository = jobSkillTreeRepository;
    }

    /**
     * Lit l'arbre de talents d'un métier.
     *
     * @param jobName le métier
     * @return l'arbre de talents, jamais {@code null}
     * @throws IllegalArgumentException si {@code jobName} est {@code null} ou si la définition de l'arbre est introuvable
     */
    public TalentTree getTalentTree(JobName jobName) {
        return jobSkillTreeRepository.getTalentTree(jobName);
    }
}

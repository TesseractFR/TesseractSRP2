package onl.tesseract.srp.job.domain.port.serverside;

import onl.tesseract.srp.job.domain.model.JobName;
import onl.tesseract.srp.job.domain.model.talenttree.TalentTree;

/**
 * Port de lecture de l'arbre de talents d'un métier, c'est-à-dire sa disposition dans le menu.
 */
public interface JobTalentTreeRepository {

    /**
     * Lit l'arbre de talents d'un métier.
     *
     * @param jobName le métier
     * @return l'arbre de talents, jamais {@code null}
     * @throws IllegalArgumentException si {@code jobName} est {@code null} ou si la définition de l'arbre est introuvable
     */
    TalentTree getTalentTree(JobName jobName);
}

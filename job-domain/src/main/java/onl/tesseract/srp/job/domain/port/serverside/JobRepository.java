package onl.tesseract.srp.job.domain.port.serverside;

import onl.tesseract.srp.job.domain.model.Job;
import onl.tesseract.srp.job.domain.model.JobName;

import java.util.List;

/**
 * Port de lecture de la définition des métiers (talents et sources exploitées).
 */
public interface JobRepository {
    /**
     * Liste tous les métiers définis.
     *
     * @return les métiers, liste vide si aucun n'est défini
     */
    List<Job> findAll();

    /**
     * Lit un métier par son nom.
     * <p>
     * Attention : renvoie aujourd'hui {@code null} si le métier est inconnu.
     *
     * @param jobName le nom du métier
     * @return le métier, ou {@code null} s'il est inconnu
     */
    Job findByName(JobName jobName);
}

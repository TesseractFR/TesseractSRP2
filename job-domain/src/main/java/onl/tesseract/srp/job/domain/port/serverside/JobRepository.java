package onl.tesseract.srp.job.domain.port.serverside;

import onl.tesseract.srp.job.domain.model.Job;

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
}

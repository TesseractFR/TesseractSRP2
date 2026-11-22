package onl.tesseract.srp.job.domain.port.userside;

import onl.tesseract.srp.job.domain.model.Job;
import onl.tesseract.srp.job.domain.model.JobName;
import onl.tesseract.srp.job.domain.model.source.Source;
import onl.tesseract.srp.job.domain.port.serverside.JobRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Donne accès au catalogue des métiers et à l'index source → métier,
 * chargés depuis le dépôt au premier besoin.
 */
public class JobCatalogService {

    private final Map<JobName, Job> jobs = new HashMap<>();
    private final Map<Source, Job> sourceJobs = new HashMap<>();
    private final JobRepository jobRepository;

    public JobCatalogService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    /**
     * Liste tous les métiers connus.
     *
     * @return les métiers chargés, liste vide s'il n'y en a aucun
     */
    public List<Job> listJobs() {
        loadJobsIfNeeded();
        return new ArrayList<>(jobs.values());
    }

    /**
     * Lit un métier par son nom directement dans le dépôt.
     *
     * @param jobName le nom du métier
     * @return le métier correspondant
     */
    public Job getJob(JobName jobName) {
        return jobRepository.findByName(jobName);
    }

    /**
     * Cherche le métier qui exploite une source donnée.
     *
     * @param source la source frappée
     * @return le métier associé, ou vide si aucun métier n'exploite cette source
     */
    public Optional<Job> findJobBySource(Source source) {
        loadJobsIfNeeded();
        return Optional.ofNullable(sourceJobs.get(source));
    }

    /**
     * Cherche un métier parmi ceux déjà chargés dans le catalogue.
     *
     * @param jobName le nom du métier
     * @return le métier, ou vide s'il est inconnu
     */
    public Optional<Job> findLoadedJob(JobName jobName) {
        loadJobsIfNeeded();
        return Optional.ofNullable(jobs.get(jobName));
    }

    private void loadJobsIfNeeded() {
        if (!jobs.isEmpty()) {
            return;
        }
        jobRepository.findAll().forEach(job -> {
            jobs.put(job.jobName(), job);
            if (job.sources() != null && job.sources().value() != null) {
                job.sources().value().forEach((source, outputs) -> sourceJobs.put(source, job));
            }
        });
    }
}

package onl.tesseract.srp.job.domain.port.userside;

import onl.tesseract.srp.job.domain.model.*;
import onl.tesseract.srp.job.domain.model.source.Source;
import onl.tesseract.srp.job.domain.model.talent.Bonus;
import onl.tesseract.srp.job.domain.port.serverside.ItemRepository;
import onl.tesseract.srp.job.domain.port.serverside.JobRepository;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class JobService {

    private final Map<JobName, Job> jobs = new HashMap<>();
    private final Map<Source, Job> sourceJobs = new HashMap<>();
    private final Random r = new Random();
    private final JobRepository jobRepository;
    private final JobPlayerProgressionService jobPlayerProgressionService;
    private final ItemRepository itemRepository;

    public JobService(JobRepository jobRepository, JobPlayerProgressionService jobPlayerProgressionService, ItemRepository itemRepository) {
        this.jobRepository = jobRepository;
        this.jobPlayerProgressionService = jobPlayerProgressionService;
        this.itemRepository = itemRepository;
    }

    public Job getJob(JobName jobName) {
        return jobRepository.findByName(jobName);
    }

    public List<Job> listJobs() {
        if (jobs.isEmpty()) {
            jobRepository.findAll().forEach(it -> {
                jobs.put(it.jobName(), it);
                if (it.sources() != null && it.sources().value() != null) {
                    it.sources().value().forEach((src, list) -> sourceJobs.put(src, it));
                }

            });
        }
        return new ArrayList<>(jobs.values());
    }

    /**
     * Détermine si une source doit produire un résultat
     *
     * @param source       la source frappée
     */
    public void process(PlayerID playerID, Source source) {
        if (source == null) return;
        if (sourceJobs.isEmpty()) {
            listJobs();
        }
        Job job = sourceJobs.get(source);
        if (job == null) return;
        List<Material> outputs = job.sources().getOutputItems(source);
        if (outputs == null || outputs.isEmpty()) return;
        outputs.forEach(
                result -> {
                    if (r.nextDouble() < getBonusValue(job.jobName(), playerID, result, Bonus.LOOT_CHANCE)) {
                        itemRepository.giveItem(playerID, result, 1, getQuality(getBonusValue(job.jobName(), playerID, result, Bonus.QUALITY)));
                    }
                }
        );

    }

    public Quality getQuality(Double bonusValue) {
        Quality q = Quality.POOR;
        while (q.ordinal() < Quality.EXCEPTIONAL.ordinal() && r.nextDouble() < bonusValue) {
            q = q.next();
        }
        return q;

    }

    public Double getBonusValue(JobName jobName, PlayerID playerID, Material item, Bonus bonus) {
        Job job = jobs.get(jobName);
        if (job == null) return 0.0;
        AtomicReference<Double> result = new AtomicReference<>(0.0);
        job.talents().get(item, bonus).forEach(
                it -> {
                    Double value = it.getValue(jobPlayerProgressionService.getTalentLevel(playerID, jobName, it.name()));
                    result.updateAndGet(v -> v + value);
                }
        );
        return result.get();
    }


}

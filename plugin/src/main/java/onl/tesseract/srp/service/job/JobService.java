package onl.tesseract.srp.service.job;

import onl.tesseract.srp.domain.item.CustomItem;
import onl.tesseract.srp.domain.item.CustomMaterial;
import onl.tesseract.srp.domain.job.BaseStat;
import onl.tesseract.srp.domain.job.EnumJob;
import onl.tesseract.srp.domain.job.Job;
import onl.tesseract.srp.domain.job.JobHarvestEvent;
import onl.tesseract.srp.repository.yaml.job.JobsConfigRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class JobService {
    private final JobsConfigRepository jobConfigRepository;
    private final PlayerJobService playerJobService;
    private final onl.tesseract.lib.event.EventService eventService;

    public JobService(JobsConfigRepository jobConfigRepository, PlayerJobService playerJobService, onl.tesseract.lib.event.EventService eventService) {
        this.jobConfigRepository = jobConfigRepository;
        this.playerJobService = playerJobService;
        this.eventService = eventService;
    }

    public Map<EnumJob, Job> getJobs() { return jobConfigRepository.getJobs(); }

    public Job getJob(EnumJob enumJob) {
        Job job = getJobs().get(enumJob);
        if (job == null) throw new IllegalArgumentException("Job " + enumJob + " not configured");
        return job;
    }

    public Job getJobByMaterial(CustomMaterial material) {
        for (Job job : getJobs().values()) {
            if (job.getMaterials().contains(material)) {
                return job;
            }
        }
        return null;
    }

    private BaseStat getBaseStat(CustomMaterial material) {
        Job job = getJobByMaterial(material);
        if (job == null) throw new IllegalArgumentException("No job for material " + material);
        BaseStat baseStat = job.getBaseStats().get(material);
        if (baseStat == null) throw new IllegalArgumentException("No baseStat for material " + material);
        return baseStat;
    }

    /**
     * Attempt to generate an item with random quality. Probability of successful
     * loot depends on loot ratio. Triggers a JobLootItemEvent in case of success.
     * @return Generated item with random quality if the loot was successful, null otherwise.
     */
    public CustomItem generateItem(UUID playerID, CustomMaterial material) {
        Job job = getJobByMaterial(material);
        if (job == null) return null;
        onl.tesseract.srp.domain.job.PlayerJobProgression playerJobProgression = playerJobService.getPlayerJobProgression(playerID);
        JobHarvestEvent event = new JobHarvestEvent(playerID, job.getEnumJob(), material);
        BaseStat baseStat = getBaseStat(material)
            .multiplyLootChance(playerJobProgression.getLootChanceBonus(event))
            .multiplyMoneyGain(playerJobProgression.getMoneyBonus(event))
            .addQualityMean(playerJobProgression.getQualityBonus(event));

        if (baseStat.randomizeLootChance()) {
            CustomItem item = new CustomItem(material, baseStat.generateQuality(), 1);
            eventService.callEvent(new JobLootItemEvent(playerID, item, baseStat.getXpGain()));
            return item;
        }
        return null;
    }
}


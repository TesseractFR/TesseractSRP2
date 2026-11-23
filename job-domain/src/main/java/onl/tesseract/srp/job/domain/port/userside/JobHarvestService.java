package onl.tesseract.srp.job.domain.port.userside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.job.domain.model.Job;
import onl.tesseract.srp.job.domain.model.source.Source;
import onl.tesseract.srp.job.domain.model.talent.Bonus;
import onl.tesseract.srp.job.domain.port.serverside.JobRewardPort;

import java.util.Random;

/** Attribue au joueur les objets obtenus en exploitant une source de métier. */
public class JobHarvestService {

    private final Random random = new Random();
    private final JobCatalogService jobCatalogService;
    private final JobTalentBonusService jobTalentBonusService;
    private final JobRewardPort jobRewardPort;

    public JobHarvestService(JobCatalogService jobCatalogService,
                             JobTalentBonusService jobTalentBonusService,
                             JobRewardPort jobRewardPort) {
        this.jobCatalogService = jobCatalogService;
        this.jobTalentBonusService = jobTalentBonusService;
        this.jobRewardPort = jobRewardPort;
    }

    /**
     * Traite l'exploitation d'une source : chaque sortie du métier fait l'objet d'un tirage
     * de chance de butin, puis l'objet obtenu reçoit une qualité tirée selon le bonus de qualité.
     * Une source nulle, inconnue ou sans sortie est ignorée.
     *
     * @param playerId le joueur
     * @param source   la source frappée
     */
    public void process(PlayerId playerId, Source source) {
        if (source == null) {
            return;
        }
        jobCatalogService.findJobBySource(source).ifPresent(job -> harvest(playerId, source, job));
    }

    /**
     * Tire une qualité : elle part de {@link Quality#POOR} et monte d'un rang à chaque tirage réussi,
     * jusqu'à {@link Quality#EXCEPTIONAL}.
     *
     * @param bonusValue la probabilité de réussite de chaque tirage
     * @return la qualité obtenue
     */
    public Quality getQuality(double bonusValue) {
        Quality quality = Quality.POOR;
        while (quality.ordinal() < Quality.EXCEPTIONAL.ordinal() && random.nextDouble() < bonusValue) {
            quality = quality.next();
        }
        return quality;
    }

    private void harvest(PlayerId playerId, Source source, Job job) {
        job.sources().getOutputItems(source).forEach(output -> {
            double lootChance = jobTalentBonusService.getBonusValue(job.jobName(), playerId, output, Bonus.LOOT_CHANCE);
            if (random.nextDouble() < lootChance) {
                double qualityBonus = jobTalentBonusService.getBonusValue(job.jobName(), playerId, output, Bonus.QUALITY);
                jobRewardPort.giveItem(playerId, output, 1, getQuality(qualityBonus));
            }
        });
    }
}

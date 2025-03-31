package onl.tesseract.srp.service.job

import onl.tesseract.lib.event.EventService
import onl.tesseract.srp.domain.item.CustomItem
import onl.tesseract.srp.domain.item.CustomMaterial
import onl.tesseract.srp.domain.job.BaseStat
import onl.tesseract.srp.domain.job.EnumJob
import onl.tesseract.srp.domain.job.Job
import onl.tesseract.srp.domain.job.JobHarvestEvent
import onl.tesseract.srp.repository.yaml.job.JobsConfigRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class JobService(
    private val jobConfigRepository: JobsConfigRepository,
    private val playerJobService: PlayerJobService,
    private val eventService: EventService,
) {
    fun getJobs(): Map<EnumJob, Job> = jobConfigRepository.getJobs()

    fun getJob(enumJob: EnumJob): Job = getJobs()[enumJob] ?: error("Job $enumJob not configured")

    fun getJobByMaterial(material: CustomMaterial): Job? {
        return getJobs().values.find { job ->
            job.materials.contains(material)
        }
    }

    private fun getBaseStat(material: CustomMaterial): BaseStat {
        val job = getJobByMaterial(material) ?: throw IllegalArgumentException("No job for material $material")
        return job.baseStats[material] ?: throw IllegalArgumentException("No baseStat for material $material")
    }

    /**
     * Attempt to generate an item with random quality. Probability of successful
     * loot depends on loot ratio. Triggers a [JobLootItemEvent] in case of success.
     * @return Generated item with random quality if the loot was successful, null otherwise.
     */
    fun generateItem(playerID: UUID, material: CustomMaterial): CustomItem? {
        val job = getJobByMaterial(material) ?: return null
        val playerJobProgression = playerJobService.getPlayerJobProgression(playerID)
        val event = JobHarvestEvent(playerID, job.enumJob, material)
        val baseStat = getBaseStat(material)
            .multiplyLootChance(playerJobProgression.getLootChanceBonus(event))
            .multiplyMoneyGain(playerJobProgression.getMoneyBonus(event))
            .addQualityMean(playerJobProgression.getQualityBonus(event))

        return if (baseStat.randomizeLootChance()) {
            val item = CustomItem(material, baseStat.generateQuality())
            eventService.callEvent(JobLootItemEvent(playerID, item, baseStat.xpGain))
            item
        } else
            null
    }
}
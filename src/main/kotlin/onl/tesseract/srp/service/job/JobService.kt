package onl.tesseract.srp.service.job

import onl.tesseract.srp.domain.item.CustomItem
import onl.tesseract.srp.domain.item.CustomMaterial
import onl.tesseract.srp.domain.job.BaseStat
import onl.tesseract.srp.domain.job.Job
import onl.tesseract.srp.domain.job.JobHarvestEvent
import onl.tesseract.srp.repository.yaml.job.JobsConfigRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class JobService(
    private val jobConfigRepository: JobsConfigRepository,
    private val playerJobService: PlayerJobService,
) {
    fun getJobs(): Map<String, Job> = jobConfigRepository.getJobs()

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
     * @return Generated item with random quality, or null if not looted
     */
    fun generateItem(playerID: UUID, material: CustomMaterial): CustomItem? {
        val job = getJobByMaterial(material) ?: return null
        val playerJobProgression = playerJobService.getPlayerJobProgression(playerID)
        val event = JobHarvestEvent(playerID, job.enumJob, material)
        val baseStat = getBaseStat(material)
            .multiplyLootChance(playerJobProgression.getLootChanceBonus(event))
            .multiplyMoneyGain(playerJobProgression.getMoneyBonus(event))
            .addQualityMean(playerJobProgression.getQualityBonus(event))

        return if (baseStat.randomizeLootChance())
            CustomItem(material, baseStat.generateQuality())
        else
            null
    }

}
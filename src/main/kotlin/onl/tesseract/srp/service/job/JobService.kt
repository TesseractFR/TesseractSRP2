package onl.tesseract.srp.service.job

import onl.tesseract.srp.domain.item.CustomItem
import onl.tesseract.srp.domain.item.CustomMaterial
import onl.tesseract.srp.domain.job.BaseStat
import onl.tesseract.srp.domain.job.Job
import onl.tesseract.srp.repository.yaml.job.JobsConfigRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class JobService(
    private val jobConfigRepository: JobsConfigRepository
) {
    private val random = Random()

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
    fun generateItem(material: CustomMaterial): CustomItem? {
        val baseStat = getBaseStat(material)
        val roll = random.nextFloat()
        return if (roll <= baseStat.lootChance) {
            val quality = generateQuality(baseStat)
            CustomItem(material, quality)
        } else {
            null
        }
    }

    private fun generateQuality(baseStat: BaseStat): Int {
        val gaussian = random.nextGaussian() * baseStat.qualityDistribution.stddev + baseStat.qualityDistribution.expectation
        return gaussian.toInt().coerceIn(1, 100)
    }
}
package onl.tesseract.srp.repository.hibernate.job

import onl.tesseract.lib.exception.ConfigurationException
import onl.tesseract.lib.logger.LoggerFactory
import onl.tesseract.srp.domain.item.CustomMaterial
import onl.tesseract.srp.domain.job.BaseStat
import onl.tesseract.srp.domain.job.Job
import onl.tesseract.srp.domain.job.QualityDistribution
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.slf4j.Logger
import org.springframework.stereotype.Component
import java.io.File

private val logger: Logger = LoggerFactory.getLogger(JobsConfigRepository::class.java)

@Component
class JobsConfigRepository {
    private var jobs: MutableMap<String, Job>? = null

    private fun loadJobs() {
        val file = File("plugins/Tesseract/jobs.yml")
        if (!file.exists()) {
            logger.error("The file jobs.yml doesn't exist!")
            return
        }

        val config = YamlConfiguration.loadConfiguration(file)
        val jobKeys = config.getConfigurationSection("jobs")?.getKeys(false) ?: emptySet()

        val loadedJobs = mutableMapOf<String, Job>()

        for (jobName in jobKeys) {
            val jobSection = config.getConfigurationSection("jobs.$jobName") ?: continue
            val baseStats = jobSection.getConfigurationSection("baseStats")?.getKeys(false)?.mapNotNull { materialName ->
                val statsSection = jobSection.getConfigurationSection("baseStats.$materialName")
                    ?: throw ConfigurationException("Missing baseStats section for material $materialName")

                try {
                    val material = CustomMaterial.valueOf(materialName)
                    material to parseBaseStat(statsSection)
                } catch (e: IllegalArgumentException) {
                    logger.warn("Material '$materialName' defined in jobs.yml doesn't exist in CustomMaterial.")
                    null
                }
            }?.toMap() ?: emptyMap()

            loadedJobs[jobName] = Job(baseStats)
        }
        jobs = loadedJobs
    }

    fun getJobs(): Map<String, Job> {
        if (jobs == null) {
            loadJobs()
        }
        return jobs ?: emptyMap()
    }

    private fun parseBaseStat(statsSection: ConfigurationSection): BaseStat {
        return try {
            val lootChance = statsSection.getDouble("lootChance")
            val moneyGain = statsSection.getInt("moneyGain")
            val xpGain = statsSection.getInt("xpGain")
            val expectation = statsSection.getInt("qualityDistribution.expectation")
            val stddev = statsSection.getDouble("qualityDistribution.stddev")

            BaseStat(lootChance.toFloat(), moneyGain, xpGain, QualityDistribution(expectation, stddev.toFloat()))

        } catch (e: ConfigurationException) {
            logger.warn("${e.message}")
            throw e
        }
    }
}

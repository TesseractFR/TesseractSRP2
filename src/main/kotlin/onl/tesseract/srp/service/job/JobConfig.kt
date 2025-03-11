package onl.tesseract.srp.service.job

import jakarta.annotation.PostConstruct
import onl.tesseract.srp.domain.item.CustomMaterial
import onl.tesseract.srp.domain.job.BaseStat
import onl.tesseract.srp.domain.job.Job
import onl.tesseract.srp.domain.job.QualityDistribution
import org.bukkit.configuration.file.YamlConfiguration
import org.springframework.stereotype.Component
import java.io.File

@Component
class JobConfig {
    private val jobs: MutableMap<String, Job> = mutableMapOf()

    @PostConstruct
    fun loadJobs() {
        val file = File("plugins/Tesseract/jobs.yml")

        val config = YamlConfiguration.loadConfiguration(file)
        val jobKeys = config.getConfigurationSection("jobs")?.getKeys(false) ?: emptySet()

        for (jobName in jobKeys) {
            val jobSection = config.getConfigurationSection("jobs.$jobName") ?: continue

            val materials = jobSection.getStringList("materials").mapNotNull { materialName ->
                CustomMaterial.entries.find {
                    it.name.equals(materialName, ignoreCase = true) ||
                            it.baseMaterial.name.equals(materialName, ignoreCase = true)
                }
            }

            val baseStats = jobSection.getConfigurationSection("baseStats")?.getKeys(false)?.mapNotNull { materialName ->
                val statsSection = jobSection.getConfigurationSection("baseStats.$materialName") ?: return@mapNotNull null
                val lootChance = statsSection.getDouble("lootChance", 0.0).toFloat()
                val moneyGain = statsSection.getInt("moneyGain", 0)
                val xpGain = statsSection.getInt("xpGain", 0)
                val expectation = statsSection.getInt("qualityDistribution.expectation", 50)
                val stddev = statsSection.getDouble("qualityDistribution.stddev", 10.0).toFloat()

                val material = CustomMaterial.entries.find { it.name.equals(materialName, ignoreCase = true) }
                material?.let { it to BaseStat(lootChance, moneyGain, xpGain, QualityDistribution(expectation, stddev)) }
            }?.toMap() ?: emptyMap()

            jobs[jobName] = Job(materials, baseStats)
        }
    }

    fun getJobs(): Map<String, Job> = jobs
}

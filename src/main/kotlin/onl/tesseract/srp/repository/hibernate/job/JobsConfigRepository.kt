package onl.tesseract.srp.repository.hibernate.job

import jakarta.annotation.PostConstruct
import onl.tesseract.srp.domain.item.CustomMaterial
import onl.tesseract.srp.domain.job.BaseStat
import onl.tesseract.srp.domain.job.Job
import onl.tesseract.srp.domain.job.QualityDistribution
import org.bukkit.configuration.file.YamlConfiguration
import org.springframework.stereotype.Component
import java.io.File

@Component
class JobsConfigRepository {
    private val jobs: MutableMap<String, Job> = mutableMapOf()

    @PostConstruct
    fun loadJobs() {
        val file = File("plugins/Tesseract/jobs.yml")
        if (!file.exists()) {
            println("The file jobs.yml doesn't exist !")
            return
        }

        val config = YamlConfiguration.loadConfiguration(file)
        val jobKeys = config.getConfigurationSection("jobs")?.getKeys(false) ?: emptySet()

        for (jobName in jobKeys) {
            val jobSection = config.getConfigurationSection("jobs.$jobName") ?: continue

            val baseStatsKeys = jobSection.getConfigurationSection("baseStats")?.getKeys(false) ?: emptySet()
            val materials = baseStatsKeys.mapNotNull { materialName ->
                CustomMaterial.entries.find {
                    it.name.equals(materialName, ignoreCase = true) ||
                            it.droppedByMaterial.name.equals(materialName, ignoreCase = true) ||
                            it.customMaterial.name.equals(materialName, ignoreCase = true)
                }
            }

            val baseStats = baseStatsKeys.mapNotNull { materialName ->
                val statsSection = jobSection.getConfigurationSection("baseStats.$materialName") ?: return@mapNotNull null
                val lootChance = statsSection.getDouble("lootChance", 0.0).toFloat()
                val moneyGain = statsSection.getInt("moneyGain", 0)
                val xpGain = statsSection.getInt("xpGain", 0)
                val expectation = statsSection.getInt("qualityDistribution.expectation", 50)
                val stddev = statsSection.getDouble("qualityDistribution.stddev", 10.0).toFloat()

                val material = CustomMaterial.entries.find {
                    it.name.equals(materialName, ignoreCase = true) ||
                            it.droppedByMaterial.name.equals(materialName, ignoreCase = true) ||
                            it.customMaterial.name.equals(materialName, ignoreCase = true)
                }
                material?.let { it to BaseStat(lootChance, moneyGain, xpGain, QualityDistribution(expectation, stddev)) }
            }.toMap()

            jobs[jobName] = Job(materials, baseStats)
        }
    }

    fun getJobs(): Map<String, Job> = jobs
}

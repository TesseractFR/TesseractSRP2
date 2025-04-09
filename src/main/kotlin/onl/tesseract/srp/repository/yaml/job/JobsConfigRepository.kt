package onl.tesseract.srp.repository.yaml.job

import onl.tesseract.lib.exception.ConfigurationException
import onl.tesseract.lib.logger.LoggerFactory
import onl.tesseract.lib.util.getSectionList
import onl.tesseract.srp.domain.item.CustomMaterial
import onl.tesseract.srp.domain.job.*
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.slf4j.Logger
import org.springframework.stereotype.Component
import java.io.File

private val logger: Logger = LoggerFactory.getLogger(JobsConfigRepository::class.java)

@Component
class JobsConfigRepository {
    private lateinit var jobs: Map<EnumJob, Job>

    private fun loadJobs(): Map<EnumJob, Job> {
        val file = File("plugins/Tesseract/jobs.yml")
        if (!file.exists()) {
            throw ConfigurationException("The file jobs.yml doesn't exist!")
        }

        val config = YamlConfiguration.loadConfiguration(file)
        val jobKeys = config.getConfigurationSection("jobs")?.getKeys(false) ?: emptySet()
        if (jobKeys.isEmpty()) {
            throw ConfigurationException("No jobs configured!")
        }

        val loadedJobs = mutableMapOf<EnumJob, Job>()

        for (jobName in jobKeys) {
            val jobSection = config.getConfigurationSection("jobs.$jobName") ?: continue
            val baseStats = jobSection.getConfigurationSection("baseStats")?.getKeys(false)?.mapNotNull { materialName ->
                try {
                    val statsSection = jobSection.getConfigurationSection("baseStats.$materialName")
                        ?: throw ConfigurationException("Missing baseStats section for material $materialName")

                    val material = CustomMaterial.valueOf(materialName)
                    material to parseBaseStat(statsSection)
                } catch (e: IllegalArgumentException) {
                    logger.warn("Material '$materialName' defined in jobs.yml doesn't exist in CustomMaterial.")
                    null
                } catch (e: ConfigurationException) {
                    logger.error("Error while parsing base stats for material $materialName in job $jobName", e)
                    null
                }
            }?.toMap() ?: emptyMap()

            val enumJob = EnumJob.valueOf(jobName)
            val missionTemplates = readMissionTemplateList(enumJob, jobSection)
            loadedJobs[enumJob] = Job(enumJob, baseStats, missionTemplates)
        }
        return loadedJobs
    }

    fun getJobs(): Map<EnumJob, Job> {
        if (!this::jobs.isInitialized) {
            try {
                jobs = loadJobs()
            } catch (e: ConfigurationException) {
                logger.error("Error while loading jobs", e)
                jobs = emptyMap()
            }
        }
        return jobs
    }

    /**
     * @throws ConfigurationException If configuration is invalid
     */
    private fun parseBaseStat(statsSection: ConfigurationSection): BaseStat {
        val lootChance = statsSection.getDouble("lootChance", -1.0)
            .also { if (it < 0) throw ConfigurationException("Missing lootChance") }
        val moneyGain = statsSection.getInt("moneyGain", -1)
            .also { if (it < 0) throw ConfigurationException("Missing moneyGain") }
        val xpGain = statsSection.getInt("xpGain", -1)
            .also { if (it < 0) throw ConfigurationException("Missing xpGain") }
        val expectation = statsSection.getInt("qualityDistribution.expectation", -1)
            .also { if (it < 0) throw ConfigurationException("Missing qualityDistribution.expectation") }
        val stddev = statsSection.getDouble("qualityDistribution.stddev", -1.0)
            .also { if (it < 0) throw ConfigurationException("Missing qualityDistribution.stddev") }

        return BaseStat(lootChance.toFloat(), moneyGain, xpGain, QualityDistribution(expectation, stddev.toFloat()))
    }

    /**
     * Parse mission templates. Will print error logs on error while parsing a template, but will continue to parse
     * next templates.
     */
    private fun readMissionTemplateList(job: EnumJob, section: ConfigurationSection): Collection<MissionTemplate> {
        val missions: Collection<MissionTemplate> = section.getSectionList("missions") {
            return@getSectionList try {
                this.readMissionTemplate(it)
            } catch (e: Exception) {
                logger.error("Failed to read mission template from config file", e)
                null
            }
        }
        if (missions.isEmpty())
            logger.warn("No missions configured for job $job. Is 'missions' section missing?")
        return missions
    }

    /**
     * @throws ConfigurationException
     */
    private fun readMissionTemplate(section: ConfigurationSection): MissionTemplate {
        val materialName = section.getString("material")
            ?: throw ConfigurationException("Missing property 'material'")

        val material = try {
            CustomMaterial.valueOf(materialName)
        } catch (e: IllegalArgumentException) {
            throw ConfigurationException("Invalid field material", e)
        }
        val quantity = section.getInt("quantity")
        val minQuality = section.getInt("minQuality")
        return try {
            MissionTemplate(material, quantity, minQuality)
        } catch (e: IllegalArgumentException) {
            throw ConfigurationException("Invalid fields for job mission template", e)
        }
    }
}
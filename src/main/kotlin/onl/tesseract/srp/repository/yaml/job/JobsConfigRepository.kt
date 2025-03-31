package onl.tesseract.srp.repository.yaml.job

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import onl.tesseract.lib.exception.ConfigurationException
import onl.tesseract.lib.logger.LoggerFactory
import onl.tesseract.srp.domain.job.EnumJob
import onl.tesseract.srp.domain.job.Job
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
        val jobs: JobsConfig = Yaml.default.decodeFromStream(file.inputStream())
        return jobs.toDomain()
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
}
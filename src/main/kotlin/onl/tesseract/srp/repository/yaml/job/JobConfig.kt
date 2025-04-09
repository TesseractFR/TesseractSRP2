package onl.tesseract.srp.repository.yaml.job

import kotlinx.serialization.Serializable
import onl.tesseract.srp.domain.item.CustomMaterial
import onl.tesseract.srp.domain.job.BaseStat
import onl.tesseract.srp.domain.job.EnumJob
import onl.tesseract.srp.domain.job.Job
import onl.tesseract.srp.domain.job.MissionTemplate

@Serializable
data class JobsConfig(
    val jobs: Map<EnumJob, JobConfig>
) {

    fun toDomain(): Map<EnumJob, Job> {
        return jobs.mapValues { (enumJob, jobConfig) -> jobConfig.toDomain(enumJob) }
    }

    @Serializable
    data class JobConfig(
        val baseStats: Map<CustomMaterial, BaseStat>,
        val missions: Collection<MissionTemplate>
    ) {

        fun toDomain(enumJob: EnumJob): Job {
            return Job(enumJob, baseStats, missions)
        }
    }
}
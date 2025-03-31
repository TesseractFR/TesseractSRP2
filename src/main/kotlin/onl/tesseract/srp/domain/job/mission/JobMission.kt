package onl.tesseract.srp.domain.job.mission

import onl.tesseract.srp.domain.item.CustomMaterial
import onl.tesseract.srp.domain.job.EnumJob

data class JobMission(
    val id: Long?,
    val job: EnumJob,
    val material: CustomMaterial,
    val quantity: Int,
    val minimalQuality: Int,
    val reward: Int
)

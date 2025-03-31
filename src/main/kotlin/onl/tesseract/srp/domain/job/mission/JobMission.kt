package onl.tesseract.srp.domain.job.mission

import onl.tesseract.srp.domain.item.CustomMaterial
import onl.tesseract.srp.domain.job.EnumJob
import java.util.*

data class JobMission(
    val id: Long,
    val playerId: UUID,
    val job: EnumJob,
    val material: CustomMaterial,
    val quantity: Int,
    val minimalQuality: Int,
    var delivered: Int = 0,
    val reward: Int
)

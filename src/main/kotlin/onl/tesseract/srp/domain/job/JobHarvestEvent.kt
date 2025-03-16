package onl.tesseract.srp.domain.job

import onl.tesseract.srp.domain.item.CustomMaterial
import java.util.UUID

data class JobHarvestEvent(val playerID: UUID, val job: EnumJob, val material: CustomMaterial)
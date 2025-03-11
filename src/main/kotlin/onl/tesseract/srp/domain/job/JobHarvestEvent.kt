package onl.tesseract.srp.domain.job

import onl.tesseract.srp.domain.item.CustomMaterial
import org.springframework.boot.autoconfigure.batch.BatchProperties.Job
import java.util.UUID

data class JobHarvestEvent(val playerID: UUID, val job: Job, val material: CustomMaterial)
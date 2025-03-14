package onl.tesseract.srp.domain.job

import onl.tesseract.srp.domain.item.CustomMaterial

data class Job(
    val baseStats: Map<CustomMaterial, BaseStat>
)


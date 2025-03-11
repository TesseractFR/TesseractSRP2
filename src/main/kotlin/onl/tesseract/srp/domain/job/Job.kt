package onl.tesseract.srp.domain.job

import onl.tesseract.srp.domain.item.CustomMaterial

data class Job(
    val materials: List<CustomMaterial>,
    val baseStats: Map<CustomMaterial, BaseStat>
)

package onl.tesseract.srp.domain.jobs

import onl.tesseract.srp.domain.item.CustomMaterial

class Job(
    val materials: List<CustomMaterial>,
    val baseStats: Map<CustomMaterial, BaseStat>
)

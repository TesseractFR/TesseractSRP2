package onl.tesseract.srp.domain.job

import onl.tesseract.srp.domain.item.CustomMaterial

data class Job(
    val enumJob: EnumJob,
    val baseStats: Map<CustomMaterial, BaseStat>
) {
    val materials: Collection<CustomMaterial>
        get() = baseStats.keys
}


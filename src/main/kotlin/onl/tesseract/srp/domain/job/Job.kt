package onl.tesseract.srp.domain.job

import onl.tesseract.srp.domain.item.CustomMaterial

data class Job(
    val enumJob: EnumJob,
    val baseStats: Map<CustomMaterial, BaseStat>,
    val missionTemplates: Collection<MissionTemplate>
) {
    val materials: Collection<CustomMaterial>
        get() = baseStats.keys
}

data class MissionTemplate(
    val items: List<MissionItem>
) {

    init {
        require(items.isNotEmpty())
    }
}

data class MissionItem(
    val material: CustomMaterial,
    val quantity: Int,
    val minQuality: Int,
) {

    init {
        require(quantity > 0)
        require(minQuality > 0)
    }
}
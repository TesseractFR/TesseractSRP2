package onl.tesseract.srp.domain.job

import onl.tesseract.srp.domain.item.CustomMaterial
import org.bukkit.Material

enum class JobSkill(
    val root: EnumJob,
    val displayName: String,
    val icon: Material,
    val parent: JobSkill?,
    val cost: Int,
    val bonus: JobBonus
) {
    MINEUR_LOOT_CHANCE_1(
        EnumJob.Mineur,
        "Chance 1",
        Material.RABBIT_FOOT,
        null,
        1,
        GenericMaterialJobBonus(CustomMaterial.Wood, JobBonusType.LootChance, 0.15f)
    ),
    MINEUR_QUALITY_1(
        EnumJob.Mineur,
        "Qualité 1",
        Material.EXPERIENCE_BOTTLE,
        null,
        1,
        GenericMaterialJobBonus(CustomMaterial.Wood, JobBonusType.Quality, 0.5f)
    ),
    MINEUR_MONEY_1(
        EnumJob.Mineur,
        "Money 1",
        Material.GOLD_NUGGET,
        null,
        1,
        GenericMaterialJobBonus(CustomMaterial.Wood, JobBonusType.Money, 0.1f)
    ),
}
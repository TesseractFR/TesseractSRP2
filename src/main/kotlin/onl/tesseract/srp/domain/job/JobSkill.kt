package onl.tesseract.srp.domain.job

import onl.tesseract.srp.domain.item.CustomMaterial

enum class JobSkill(val root: EnumJob, val parent: JobSkill?, val cost: Int, val bonus: JobBonus) {
    MINEUR_LOOT_CHANCE_1(EnumJob.Mineur, null, 1, GenericMaterialJobBonus(CustomMaterial.Wood, JobBonusType.LootChance, 0.15f))
}
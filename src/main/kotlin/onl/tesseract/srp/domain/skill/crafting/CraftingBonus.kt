package onl.tesseract.srp.domain.skill.crafting

data class CraftingBonus(
    val qualityBonus: Double = 0.0,
    val successBonus: Double = 0.0,
    val doubleCraftBonus : Double = 0.0,
    val garbageRefundBonus: Double = 0.0,
    val craftRefundBonus: Double = 0.0
)

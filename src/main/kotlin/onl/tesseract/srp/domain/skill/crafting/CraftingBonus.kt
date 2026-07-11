package onl.tesseract.srp.domain.skill.crafting

import onl.tesseract.srp.skill.domain.model.bonus.Bonus

data class CraftingBonus(
    val qualityBonus: Bonus = Bonus(0.0),
    val successBonus: Bonus = Bonus(0.0),
    val garbageRefundBonus: Bonus = Bonus(0.0),
    val craftRefundBonus: Bonus = Bonus(0.0),
    val timeReduction: Bonus = Bonus(0.0),
    val recoverySuccessBonus: Bonus = Bonus(0.0),
    val recoveryFailureBonus: Bonus = Bonus(0.0)
)

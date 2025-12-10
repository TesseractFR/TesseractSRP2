package onl.tesseract.srp.service.equipment.elytra

import onl.tesseract.lib.event.equipment.invocable.ElytraUpgrade

data class ElytraUpgradeStats(
    val currentValue: Double,
    val nextValue: Double?,
    val unit: String,
    val type: ElytraUpgrade
)

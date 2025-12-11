package onl.tesseract.srp.domain.equipment.elytra

import onl.tesseract.lib.event.equipment.invocable.ElytraUpgrade

data class ElytraUpgradeStats(
    val currentValue: Double,
    val nextValue: Double?,
    val type: ElytraUpgrade
)

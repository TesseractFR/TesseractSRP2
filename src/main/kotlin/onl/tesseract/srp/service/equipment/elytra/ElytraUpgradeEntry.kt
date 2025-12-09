package onl.tesseract.srp.service.equipment.elytra

import onl.tesseract.lib.event.equipment.invocable.ElytraUpgrade

data class ElytraUpgradeEntry(
    val upgrade: ElytraUpgrade,
    val currentLevel: Int,
    val nextLevel: Int?,
    val maxLevel: Int,
    val price: Int?,
    val canAfford: Boolean
)
package onl.tesseract.srp.service.equipment.elytra

import onl.tesseract.lib.event.equipment.invocable.EnumElytraUpgrade

data class ElytraUpgradeEntry(
    val upgrade: EnumElytraUpgrade,
    val currentLevel: Int,
    val nextLevel: Int?,
    val maxLevel: Int,
    val price: Int?,
    val canAfford: Boolean
)
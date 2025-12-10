package onl.tesseract.srp.service.equipment.elytra

data class ElytraUpgradeMenuState(
    val hasElytra: Boolean,
    val money: Int,
    val illuminationPoints: Int,
    val rankLabel: String,
    val entries: List<ElytraUpgradeEntry>
)

package onl.tesseract.srp.service.equipment.elytra

data class ElytraMenuState(
    val hasElytra: Boolean,
    val isInvoked: Boolean = false,
    val autoGlide: Boolean = false
)
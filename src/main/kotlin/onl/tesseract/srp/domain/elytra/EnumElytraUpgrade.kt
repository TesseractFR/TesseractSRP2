package onl.tesseract.srp.domain.elytra

import org.bukkit.Material

enum class EnumElytraUpgrade(val displayName: String, val material: Material) {
    SPEED("Amélioration : Vitesse", Material.SUGAR),
    PROTECTION("Amélioration : Protection", Material.SHIELD);
}

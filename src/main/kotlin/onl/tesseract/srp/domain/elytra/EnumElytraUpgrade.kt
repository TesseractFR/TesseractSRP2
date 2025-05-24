package onl.tesseract.srp.domain.elytra

import org.bukkit.Material

enum class EnumElytraUpgrade(val displayName: String, val material: Material) {
    SPEED("Amélioration : Vitesse", Material.SUGAR),
    PROTECTION("Amélioration : Protection", Material.SHIELD),
    BOOST_CHARGE("Amélioration : Charges de Boost Maximum", Material.FEATHER),
    RECOVERY("Amélioration : Récupération", Material.CLOCK)
}

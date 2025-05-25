package onl.tesseract.srp.domain.elytra

import org.bukkit.Material

enum class EnumElytraUpgrade(val displayName: String, val description: String, val material: Material) {
    SPEED("Vitesse",
        "Augmente la vitesse de vol maximale globale",
        Material.SUGAR),
    PROTECTION("Protection",
        "Ajoute des points d'armure sur les élytras (+0.5 pts par niveau)",
        Material.SHIELD),
    BOOST_CHARGE("Charges de Boost Maximum",
        "Augmente le nombre maximal de charges de boost de vitesse",
        Material.FEATHER),
    RECOVERY("Rechargement de Boosts",
        "Augmente la vitesse de rechargement des boosts de vitesse",
        Material.CLOCK)
}

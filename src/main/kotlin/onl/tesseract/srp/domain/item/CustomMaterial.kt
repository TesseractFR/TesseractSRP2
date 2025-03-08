package onl.tesseract.srp.domain.item

import org.bukkit.Material

enum class CustomMaterial(
    val baseMaterial: Material,
    val displayName: String,
    val rarity: Rarity,
) {
    Wood(Material.OAK_LOG, displayName = "Bois", rarity = Rarity.Common),
}
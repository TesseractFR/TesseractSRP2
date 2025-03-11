package onl.tesseract.srp.domain.item

import org.bukkit.Material

enum class CustomMaterial(
    val droppedByMaterial : Material,
    val customMaterial: Material,
    val displayName: String,
    val rarity: Rarity,
) {
    Wood(Material.OAK_LOG, Material.OAK_LOG, displayName = "Bois", rarity = Rarity.Common),
    BirchWood(Material.BIRCH_LOG, Material.BIRCH_LOG, displayName = "Bois de bouleau", Rarity.Common),
}
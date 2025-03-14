package onl.tesseract.srp.domain.item

import org.bukkit.Material
import org.bukkit.entity.EntityType

sealed interface CustomMaterialSource

data class CustomMaterialBlockSource(val material: Material) : CustomMaterialSource

data class CustomMaterialEntitySource(val entityType: EntityType) : CustomMaterialSource

enum class CustomMaterial(
    val dropSource: CustomMaterialSource,
    val customMaterial: Material,
    val displayName: String,
    val rarity: Rarity,
) {
    Wood(CustomMaterialBlockSource(Material.OAK_LOG), Material.OAK_LOG, "Bois", Rarity.Common),
    BirchWood(CustomMaterialBlockSource(Material.BIRCH_LOG), Material.BIRCH_LOG, "Bois de bouleau", Rarity.Common),

    ZombieDrop(CustomMaterialEntitySource(EntityType.ZOMBIE), Material.ROTTEN_FLESH, "Chair CUstom", Rarity.Rare),
    SkeletonDrop(CustomMaterialEntitySource(EntityType.SKELETON), Material.BONE, "Os Custom", Rarity.Rare)
}

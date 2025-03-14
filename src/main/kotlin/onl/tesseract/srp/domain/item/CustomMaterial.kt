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
    Wood(Material.OAK_LOG, Material.OAK_LOG, "Bois", Rarity.Common),
    BirchWood(Material.BIRCH_LOG, Material.BIRCH_LOG, "Bois de bouleau", Rarity.Common),

    ZombieDrop(EntityType.ZOMBIE, Material.ROTTEN_FLESH, "Chair Custom", Rarity.Rare),
    SkeletonDrop(EntityType.SKELETON, Material.BONE, "Os Custom", Rarity.Rare);

    constructor(material: Material, customMaterial: Material, displayName: String, rarity: Rarity)
            : this(CustomMaterialBlockSource(material), customMaterial, displayName, rarity)

    constructor(entityType: EntityType, customMaterial: Material, displayName: String, rarity: Rarity)
            : this(CustomMaterialEntitySource(entityType), customMaterial, displayName, rarity)

}

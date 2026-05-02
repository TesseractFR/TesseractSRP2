package onl.tesseract.srp.domain.item

import kotlinx.serialization.Serializable
import onl.tesseract.srp.domain.item.CustomItemIds.STEEL_INGOT
import org.bukkit.Material
import org.bukkit.entity.EntityType

sealed interface CustomMaterialSource

data class CustomMaterialBlockSource(val material: Material) : CustomMaterialSource

data class CustomMaterialEntitySource(val entityType: EntityType) : CustomMaterialSource

@Serializable
enum class CustomMaterial(
    val displayName: String,
    val itemTag: String,
    val dropSource: List<CustomMaterialSource>,
    val rarity: Rarity
){
    STEEL("Acier",STEEL_INGOT,emptyList(), Rarity.Common)
}

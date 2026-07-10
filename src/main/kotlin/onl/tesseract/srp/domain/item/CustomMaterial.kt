package onl.tesseract.srp.domain.item

import kotlinx.serialization.Serializable
import onl.tesseract.srp.customitem.domain.model.MaterialName
import org.bukkit.Material
import org.bukkit.entity.EntityType

sealed interface CustomMaterialSource

data class CustomMaterialBlockSource(val material: Material) : CustomMaterialSource

data class CustomMaterialEntitySource(val entityType: EntityType) : CustomMaterialSource

@Serializable
enum class CustomMaterial(
    val displayName: String,
    val materialName: MaterialName,
    val dropSource: List<CustomMaterialSource>,
    val rarity: Rarity
){
    STEEL("Acier", MaterialName("steel_ingot"),emptyList(), Rarity.Common);


    companion object {
        fun getByMaterialName(materialName: MaterialName): CustomMaterial? {
            return entries.find { it.materialName.value == materialName.value }
        }
    }
}

package onl.tesseract.srp.service.item

import onl.tesseract.lib.persistantcontainer.NamedspacedKeyProvider
import onl.tesseract.srp.domain.item.CustomItem
import onl.tesseract.srp.domain.item.CustomMaterial
import onl.tesseract.srp.domain.item.Quality
import onl.tesseract.srp.domain.port.CustomItemGatewayPort
import onl.tesseract.srp.domain.skill.recipe.ComponentWrapper
import onl.tesseract.srp.domain.skill.recipe.CustomComponentWrapper
import onl.tesseract.srp.domain.skill.recipe.VanillaComponentWrapper
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.springframework.stereotype.Service

@Service
class CustomItemService(
    private val namespacedKeyProvider: NamedspacedKeyProvider,
    private val customItemGatewayPort: CustomItemGatewayPort
) {
    fun getCustomItem(namespaceId: String) : ItemStack{
        return customItemGatewayPort.getCustomItem(namespaceId)
    }

    fun toItemstack(material: onl.tesseract.srp.skill.domain.model.recipe.Material): ItemStack {
        try {
            return ItemStack.of(Material.valueOf(material.value()))
        }
        catch (e: IllegalArgumentException){
            return this.toItemstack(CustomMaterial.valueOf(material.value()))
        }
        catch (e: Exception){
            throw IllegalArgumentException("Material non pris en charge")
        }
    }


    fun toItemstack(customItem: CustomItem): ItemStack {
        return toItemstack(customItem.material, customItem.quality).asQuantity(customItem.quantity)
    }

    fun toItemstack(customMaterial: CustomMaterial,quality: Quality? = null): ItemStack {
        val item =  getCustomItem(customMaterial.itemTag)
        item.editMeta {
            val dataContainer = it.persistentDataContainer
            dataContainer[namespacedKeyProvider.get("customMaterial"), PersistentDataType.STRING] = customMaterial.name
            quality.let {
                dataContainer[namespacedKeyProvider.get("quality"), PersistentDataType.STRING] = quality.toString()
            }
        }
        return item
    }

    fun isCustomItem(itemStack: ItemStack): Boolean {
        return itemStack.itemMeta
            ?.persistentDataContainer
            ?.has(namespacedKeyProvider.get("customMaterial")) ?: false
    }

    fun getCustomItemStack(itemStack: ItemStack): CustomItem? {
        if(!isCustomItem(itemStack))return null
        val dataContainer = itemStack.itemMeta?.persistentDataContainer!!
        val mat = CustomMaterial.valueOf(dataContainer[namespacedKeyProvider.get("customMaterial"), PersistentDataType.STRING]!!)
        val quality = Quality.valueOf(dataContainer[namespacedKeyProvider.get("quality"), PersistentDataType.STRING]!!)
        return CustomItem(mat,quality,itemStack.amount)
    }

    fun toItemstack(componentWrapper: ComponentWrapper): ItemStack {
        if(componentWrapper is CustomComponentWrapper){
            return this.toItemstack(componentWrapper.customMaterial, null)
        }
        else if( componentWrapper is VanillaComponentWrapper){
            return ItemStack.of(componentWrapper.material)
        }
        throw IllegalArgumentException("ComponentWrapper non pris en charge")
    }

    fun removeCustomItems(inventory: Inventory, material: CustomMaterial, minQuality: Quality, amountToRemove: Int): Int {
        var remaining = amountToRemove
        var removed = 0

        inventory.contents.forEachIndexed { index, item ->
            if (remaining <= 0) return@forEachIndexed
            if (item != null && isCustomItem(item)) {
                val stack = getCustomItemStack(item)
                if (stack != null && stack.quality >= minQuality) {
                    val amount = item.amount
                    val toRemove = minOf(remaining, amount)

                    if (toRemove >= amount) {
                        inventory.setItem(index, null)
                    } else {
                        item.amount -= toRemove
                    }

                    removed += toRemove
                    remaining -= toRemove
                }
            }
        }

        return removed
    }
}
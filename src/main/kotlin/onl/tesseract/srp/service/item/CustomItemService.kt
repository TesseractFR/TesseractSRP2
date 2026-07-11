package onl.tesseract.srp.service.item

import onl.tesseract.lib.persistantcontainer.NamedspacedKeyProvider
import onl.tesseract.srp.customitem.domain.model.MaterialName
import onl.tesseract.srp.customitem.domain.model.Quality
import onl.tesseract.srp.domain.item.CustomItem
import onl.tesseract.srp.domain.item.CustomMaterial
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.springframework.stereotype.Service

@Service
class CustomItemService(
    private val namespacedKeyProvider: NamedspacedKeyProvider
) {


    fun isCustomItem(itemStack: ItemStack): Boolean {
        return itemStack.itemMeta
            ?.persistentDataContainer
            ?.has(namespacedKeyProvider.get("customMaterial")) ?: false
    }

    fun getCustomItemStack(itemStack: ItemStack): CustomItem? {
        if(!isCustomItem(itemStack))return null
        val dataContainer = itemStack.itemMeta?.persistentDataContainer!!
        val mat = MaterialName(dataContainer[namespacedKeyProvider.get("customMaterial"), PersistentDataType.STRING]!!)
        val quality = Quality.valueOf(dataContainer[namespacedKeyProvider.get("quality"), PersistentDataType.STRING]!!)
        return CustomItem(CustomMaterial.getByMaterialName(mat)!!, quality, itemStack.amount)
    }

    fun removeCustomItems(inventory: Inventory, material: MaterialName, minQuality: Quality, amountToRemove: Int): Int {
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
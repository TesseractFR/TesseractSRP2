package onl.tesseract.srp.service.item

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.persistantcontainer.NamedspacedKeyProvider
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.domain.item.CustomItem
import onl.tesseract.srp.domain.item.CustomItemStack
import onl.tesseract.srp.domain.item.CustomMaterial
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.springframework.stereotype.Service
import onl.tesseract.srp.domain.port.CustomItemGatewayPort

@Service
class CustomItemService(
    private val namespacedKeyProvider: NamedspacedKeyProvider,
    private val customItemGatewayPort: CustomItemGatewayPort
) {
    fun getCustomItem(namespaceId: String) : ItemStack{
        return customItemGatewayPort.getCustomItem(namespaceId)
    }

    fun createCustomItem(customItem: CustomItemStack): ItemStack {
        val item = ItemBuilder(customItem.item.material.customMaterial)
            .name(customItem.item.material.displayName)
            .color(NamedTextColor.GREEN)
            .lore()
            .newline()
            .append(NamedTextColor.GRAY + "Objet de métier")
            .newline()
            .append(NamedTextColor.GRAY + "Qualité : " + (getQualityColorGradient(customItem.item.quality) + "${customItem.item.quality}%"))
            .buildLore()
            .amount(customItem.amount)
            .build()
        item.editMeta {
            val dataContainer = it.persistentDataContainer
            dataContainer[namespacedKeyProvider.get("customMaterial"), PersistentDataType.STRING] = customItem.item.material.name
            dataContainer[namespacedKeyProvider.get("quality"), PersistentDataType.INTEGER] = customItem.item.quality
        }
        return item
    }

    fun isCustomItem(itemStack: ItemStack): Boolean {
        return itemStack.itemMeta
            ?.persistentDataContainer
            ?.has(namespacedKeyProvider.get("customMaterial")) ?: false
    }

    private fun getQualityColorGradient(quality: Int): TextColor {
        val ratio: Float = quality.coerceAtMost(80) / 80.0f
        return TextColor.color(1.0f - ratio, ratio, 0.0f)
    }

    fun getCustomItemStack(itemStack: ItemStack): CustomItemStack? {
        val meta = itemStack.itemMeta ?: return null
        val container = meta.persistentDataContainer

        val quality = container.get(namespacedKeyProvider.get("quality"), PersistentDataType.INTEGER) ?: return null
        val materialName = container.get(namespacedKeyProvider.get("customMaterial"), PersistentDataType.STRING) ?: return null

        val material = runCatching { CustomMaterial.valueOf(materialName) }.getOrNull() ?: return null

        return CustomItemStack(CustomItem(material, quality), itemStack.amount)
    }


    private fun isCustomItem(itemStack: ItemStack?, material: CustomMaterial): Boolean {
        if (itemStack == null || itemStack.type != material.customMaterial) return false
        return isCustomItem(itemStack)
    }

    fun removeCustomItems(inventory: Inventory, material: CustomMaterial, minQuality: Int, amountToRemove: Int): Int {
        var remaining = amountToRemove
        var removed = 0

        inventory.contents.forEachIndexed { index, item ->
            if (remaining <= 0) return@forEachIndexed
            if (isCustomItem(item, material)) {
                val stack = getCustomItemStack(item!!)
                if (stack != null && stack.item.quality >= minQuality) {
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
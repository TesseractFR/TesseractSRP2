package onl.tesseract.srp.service.item

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.persistantcontainer.NamedspacedKeyProvider
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.domain.item.CustomItemStack
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.springframework.stereotype.Service

@Service
class CustomItemService(
    private val namespacedKeyProvider: NamedspacedKeyProvider,
) {

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
            dataContainer.set(namespacedKeyProvider.get("customMaterial"), PersistentDataType.STRING, customItem.item.material.name)
            dataContainer.set(namespacedKeyProvider.get("quality"), PersistentDataType.INTEGER, customItem.item.quality)
        }
        return item
    }

    private fun getQualityColorGradient(quality: Int): TextColor {
        val ratio: Float = quality.coerceAtMost(80) / 80.0f
        return TextColor.color(1.0f - ratio, ratio, 0.0f)
    }
}
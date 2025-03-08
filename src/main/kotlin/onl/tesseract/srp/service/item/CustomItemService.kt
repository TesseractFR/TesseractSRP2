package onl.tesseract.srp.service.item

import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.persistantcontainer.NamedspacedKeyProvider
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.domain.item.CustomMaterial
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.springframework.stereotype.Service

@Service
class CustomItemService(private val namespacedKeyProvider: NamedspacedKeyProvider) {

    fun createCustomItem(material: CustomMaterial, amount: Int = 1): ItemStack {
        val item = ItemBuilder(material.baseMaterial)
            .name(material.displayName)
            .color(NamedTextColor.GREEN)
            .lore()
            .newline()
            .append(NamedTextColor.GRAY + "Objet de métier")
            .buildLore()
            .amount(amount)
            .build()
        item.editMeta {
            it.persistentDataContainer.set(namespacedKeyProvider.get("customMaterial"), PersistentDataType.STRING, material.name)
        }
        return item
    }
}
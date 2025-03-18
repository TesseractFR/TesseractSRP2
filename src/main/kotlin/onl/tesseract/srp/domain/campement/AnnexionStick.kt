package onl.tesseract.srp.domain.campement

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import onl.tesseract.srp.PLUGIN_INSTANCE
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType

object AnnexationStick {

    private val KEY: NamespacedKey by lazy { NamespacedKey(PLUGIN_INSTANCE, "annexation_stick") }

    fun create(): ItemStack {
        val item = ItemStack(Material.STICK)
        val meta: ItemMeta = item.itemMeta!!

        meta.displayName(
            Component.text("Bâton d'Annexion")
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false)
        )

        meta.lore(listOf(
            Component.text("► ")
                .color(NamedTextColor.GRAY)
                .append(Component.text("Clic DROIT")
                    .color(NamedTextColor.GREEN)
                )
                .append(Component.text(" pour annexer un chunk !")
                    .color(NamedTextColor.GRAY)
                ),

            Component.text("► ")
                .color(NamedTextColor.GRAY)
                .append(Component.text("Clic GAUCHE")
                    .color(NamedTextColor.RED)
                )
                .append(Component.text(" pour le désannexer !")
                    .color(NamedTextColor.GRAY)
                )
        ))

        meta.addEnchant(Enchantment.MENDING, 1, true)
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)

        meta.persistentDataContainer.set(KEY, PersistentDataType.BYTE, 1)

        item.itemMeta = meta
        return item
    }

    fun isAnnexationStick(item: ItemStack?): Boolean {
        if (item == null || !item.hasItemMeta()) return false
        return item.itemMeta!!.persistentDataContainer.has(KEY, PersistentDataType.BYTE)
    }
}

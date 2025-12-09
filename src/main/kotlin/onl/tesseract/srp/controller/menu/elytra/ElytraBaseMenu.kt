package onl.tesseract.srp.controller.menu.elytra

import net.kyori.adventure.text.Component
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import org.bukkit.Material

abstract class ElytraBaseMenu(
    size: MenuSize,
    title: Component,
    previous: Menu? = null
) : Menu(size, title, previous) {

    @Suppress("MagicNumber")
    protected fun placeDecorations() {
        val graySlots = listOf(0, 1, 7, 8, 11, 13, 15, 21, 23, 29, 31, 33, 36, 37, 43, 44)
        val purpleSlots = listOf(2, 4, 6, 10, 16, 18, 26, 28, 34, 38, 40, 42)
        val cyanSlots = listOf(3, 5, 9, 12, 14, 17, 19, 20, 22, 24, 25, 27, 30, 32, 35, 39, 41)

        graySlots.forEach { slot ->
            addButton(slot, ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(Component.text(" ")).build())
        }

        purpleSlots.forEach { slot ->
            addButton(slot, ItemBuilder(Material.PURPLE_STAINED_GLASS_PANE).name(Component.text(" ")).build())
        }

        cyanSlots.forEach { slot ->
            addButton(slot, ItemBuilder(Material.CYAN_STAINED_GLASS_PANE).name(Component.text(" ")).build())
        }
    }
}
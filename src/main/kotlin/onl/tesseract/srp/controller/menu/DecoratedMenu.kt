package onl.tesseract.srp.controller.menu

import net.kyori.adventure.text.Component
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import org.bukkit.Material

abstract class DecoratedMenu(
    size: MenuSize,
    title: Component,
    previous: Menu? = null
) : Menu(size, title, previous) {
    open val graySlots: List<Int> = emptyList()
    open val purpleSlots: List<Int> = emptyList()
    open val cyanSlots: List<Int> = emptyList()
    open val greenSlots: List<Int> = emptyList()
    open val limeSlots: List<Int> = emptyList()
    open val whiteSlots: List<Int> = emptyList()
    open val lightBlueSlots: List<Int> = emptyList()
    open val blueSlots: List<Int> = emptyList()

    protected fun placeDecorations() {
        val slotsByMaterial = mapOf(
            Material.GRAY_STAINED_GLASS_PANE   to graySlots,
            Material.PURPLE_STAINED_GLASS_PANE to purpleSlots,
            Material.CYAN_STAINED_GLASS_PANE   to cyanSlots,
            Material.GREEN_STAINED_GLASS_PANE  to greenSlots,
            Material.LIME_STAINED_GLASS_PANE   to limeSlots,
            Material.WHITE_STAINED_GLASS_PANE  to whiteSlots,
            Material.LIGHT_BLUE_STAINED_GLASS_PANE to lightBlueSlots,
            Material.BLUE_STAINED_GLASS_PANE to blueSlots,
        )
        slotsByMaterial.forEach { (material, slots) ->
            slots.forEach { slot ->
                addButton(slot, ItemBuilder(material).name(Component.text(" ")).build())
            }
        }
    }
}
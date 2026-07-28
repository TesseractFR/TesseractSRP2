package onl.tesseract.srp.controller.menu.campement

import net.kyori.adventure.text.Component
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.srp.controller.menu.DecoratedMenu

@Suppress("MagicNumber")
abstract class CampementBaseMenu(
    size: MenuSize,
    title: Component,
    previous: Menu? = null
) : DecoratedMenu(size, title, previous) {
    override val blueSlots = listOf(0, 8, 18, 26)
    override val cyanSlots = listOf(1, 7, 9, 17, 19, 25)
    override val lightBlueSlots = listOf(2, 6, 20, 24, 3, 5, 21, 23)
    override val graySlots = listOf(4, 22, 11, 13, 15)
}
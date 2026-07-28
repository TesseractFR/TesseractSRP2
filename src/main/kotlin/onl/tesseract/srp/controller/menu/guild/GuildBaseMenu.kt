package onl.tesseract.srp.controller.menu.guild

import net.kyori.adventure.text.Component
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.srp.controller.menu.DecoratedMenu

@Suppress("MagicNumber")
abstract class GuildBaseMenu(
    size: MenuSize,
    title: Component,
    previous: Menu? = null
) : DecoratedMenu(size, title, previous) {
    override val limeSlots = listOf(2, 4, 6, 10, 16, 18, 26, 28, 34, 38, 40, 42)
    override val greenSlots = listOf(3, 5, 9, 12, 14, 17, 19, 20, 22, 24, 25, 27, 30, 32, 35, 39, 41)
    override val graySlots = listOf(0, 1, 7, 8, 11, 13, 15, 21, 23, 29, 31, 33, 36, 37, 43, 44)
}
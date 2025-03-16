package onl.tesseract.srp.util.menu

import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.util.toComponent
import org.bukkit.Material
import org.bukkit.entity.Player

class BiMenuTest : BiMenu(MenuSize.Six, "Test".toComponent()) {

    override fun placeButtons(viewer: Player) {
        addButton(15, ItemBuilder(Material.STONE_BUTTON)
            .name("Test")
            .build()) {

            viewer.sendMessage("Top Inventory")
            close()
        }
    }

    override fun placeBottomButtons(viewer: Player) {
        addBottomButton(3, ItemBuilder(Material.OAK_LOG)
            .name("Coucou")
            .build()) {

            viewer.sendMessage("Bottom Inventory")
            close()
        }
    }
}
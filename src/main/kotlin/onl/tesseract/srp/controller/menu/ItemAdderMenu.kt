package onl.tesseract.srp.controller.menu

import dev.lone.itemsadder.api.FontImages.FontImageWrapper
import dev.lone.itemsadder.api.FontImages.TexturedInventoryWrapper
import onl.tesseract.lib.menu.AButton
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.service.PluginService
import onl.tesseract.lib.service.ServiceContainer
import onl.tesseract.lib.util.toComponent
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType

abstract class ItemAdderMenu(size:MenuSize,val titleS: String,type: InventoryType,previous:Menu) : Menu(size, titleS.toComponent(), previous, type = type){
    private val buttons: MutableMap<Int, AButton> = mutableMapOf()
    override fun open(viewer: Player) {
        val inventory = TexturedInventoryWrapper(null,type!!,titleS, FontImageWrapper(""),1,1).internal
        this.view = viewer.openInventory(inventory)
        this.viewer = viewer
        ServiceContainer[PluginService::class.java].registerEventListener(this)
        buttons.forEach { index, button -> button.draw(this, index) }
        placeButtons(viewer)
    }
}
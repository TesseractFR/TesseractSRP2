package onl.tesseract.srp.controller.menu

import dev.lone.itemsadder.api.FontImages.FontImageWrapper
import dev.lone.itemsadder.api.FontImages.TexturedInventoryWrapper
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.util.toComponent
import org.bukkit.entity.Player

abstract class ItemAdderMenu(size:MenuSize,
                             val backgroundNamespaceId: String,
                             val titleS: String,
                             previous:Menu?) : Menu(size, titleS.toComponent(), previous){
    override fun open(viewer: Player) {
        super.open(viewer)
        TexturedInventoryWrapper.setPlayerInventoryTexture(viewer, FontImageWrapper(backgroundNamespaceId),titleS,10,-8)
    }
}
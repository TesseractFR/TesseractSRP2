package onl.tesseract.srp.controller.menu

import dev.lone.itemsadder.api.FontImages.FontImageWrapper
import dev.lone.itemsadder.api.FontImages.TexturedInventoryWrapper
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.util.toComponent
import onl.tesseract.srp.util.menu.BiMenu
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

abstract class ItemAdderBiMenu(size:MenuSize,
                             val backgroundNamespaceId: String,
                             val titleS: String,
                             previous:Menu?,val titleOffset : Int = 10) : BiMenu(size, titleS.toComponent(), previous){
    override fun open(viewer: Player) {
        super.open(viewer)
        TexturedInventoryWrapper.setPlayerInventoryTexture(viewer, FontImageWrapper(backgroundNamespaceId),titleS,titleOffset,-8)
    }
}
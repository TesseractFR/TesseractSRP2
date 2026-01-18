package onl.tesseract.srp.domain.port

import org.bukkit.inventory.ItemStack

interface CustomItemGatewayPort {
    fun getCustomItem(namespaceId: String): ItemStack
}
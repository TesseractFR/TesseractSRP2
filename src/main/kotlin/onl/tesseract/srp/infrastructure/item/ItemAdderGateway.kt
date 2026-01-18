package onl.tesseract.srp.infrastructure.item

import dev.lone.itemsadder.api.CustomStack
import onl.tesseract.srp.domain.port.CustomItemGatewayPort
import org.bukkit.inventory.ItemStack
import org.springframework.stereotype.Component

@Component
class ItemAdderGateway : CustomItemGatewayPort {
    override fun getCustomItem(namespaceId: String): ItemStack {
        val customItem =CustomStack.getInstance(namespaceId)
        requireNotNull(customItem){
            error("Impossible de charger $namespaceId")
        }
        val item = customItem.itemStack
        return item
    }
}
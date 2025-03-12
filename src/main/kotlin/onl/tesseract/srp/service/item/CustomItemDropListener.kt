package onl.tesseract.srp.service.item

import onl.tesseract.srp.domain.item.CustomItemStack
import onl.tesseract.srp.domain.item.CustomMaterial
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.springframework.stereotype.Component

@Component
class CustomItemDropListener(private val customItemService: CustomItemService) : Listener {

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player
        val block = event.block
        val material = block.type

        val customMaterial = CustomMaterial.entries.find { it.dropSource == material }

        if (customMaterial != null) {
            val customItem = customItemService.attemptDrop(customMaterial)
            if (customItem != null) {
                player.world.dropItemNaturally(block.location, customItemService.createCustomItem(CustomItemStack(customItem, 1)))
            }
        }
    }

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val entity = event.entity
        val customMaterial = CustomMaterial.entries.find { it.dropSource == entity.type }

        if (customMaterial != null) {
            val customItem = customItemService.attemptDrop(customMaterial)
            if (customItem != null) {
                val itemStack = customItemService.createCustomItem(CustomItemStack(customItem, 1))
                entity.world.dropItemNaturally(entity.location, itemStack)
            }
        }
    }
}

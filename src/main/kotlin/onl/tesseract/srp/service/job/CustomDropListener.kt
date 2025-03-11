package onl.tesseract.srp.service.job

import onl.tesseract.srp.domain.item.CustomItemStack
import onl.tesseract.srp.domain.item.CustomMaterial
import onl.tesseract.srp.service.item.CustomItemService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.springframework.stereotype.Component

@Component
class CustomDropListener(private val customItemService: CustomItemService) : Listener {

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player
        val block = event.block
        val material = block.type

        val customMaterial = CustomMaterial.entries.find { it.baseMaterial == material }
        if (customMaterial != null) {
            val customItem = customItemService.attemptDrop(customMaterial)
            if (customItem != null) {
                player.world.dropItemNaturally(block.location, customItemService.createCustomItem(CustomItemStack(customItem, 1)))
            }
        }
    }

}

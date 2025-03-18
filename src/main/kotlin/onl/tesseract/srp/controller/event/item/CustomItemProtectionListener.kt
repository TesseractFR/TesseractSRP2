package onl.tesseract.srp.controller.event.item

import onl.tesseract.lib.util.plus
import onl.tesseract.srp.service.item.CustomItemService
import onl.tesseract.srp.util.jobsChatFormatError
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.CraftingInventory
import org.springframework.stereotype.Component

@Component
class CustomItemProtectionListener(private val customItemService: CustomItemService) : Listener {

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        if (customItemService.isCustomItem(event.itemInHand)) {
            event.isCancelled = true
            event.player.sendMessage(jobsChatFormatError + "Les objets de métier ne peuvent pas être posés.")
        }
    }

    @EventHandler
    fun onEat(event: PlayerItemConsumeEvent) {
        if (customItemService.isCustomItem(event.item)) {
            event.isCancelled = true
            event.player.sendMessage(jobsChatFormatError + "Les objets de métier ne peuvent pas être consommés.")
        }
    }

    @EventHandler
    fun onPrepareCraftItem(event: PrepareItemCraftEvent) {
        if (event.recipe == null) return
        val topInventory = event.view.topInventory
        if (topInventory is CraftingInventory) {
            val hasCustomItem = topInventory.contents.filterNotNull().any { customItemService.isCustomItem(it) }
            if (hasCustomItem) {
                topInventory.result = null
            }
        }
    }
}
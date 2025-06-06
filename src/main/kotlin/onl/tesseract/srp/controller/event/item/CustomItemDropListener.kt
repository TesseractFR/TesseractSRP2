package onl.tesseract.srp.controller.event.item

import onl.tesseract.lib.task.TaskScheduler
import onl.tesseract.srp.domain.item.CustomItemStack
import onl.tesseract.srp.domain.item.CustomMaterial
import onl.tesseract.srp.domain.item.CustomMaterialBlockSource
import onl.tesseract.srp.domain.item.CustomMaterialEntitySource
import onl.tesseract.srp.service.item.CustomItemService
import onl.tesseract.srp.service.job.JobService
import onl.tesseract.srp.service.world.WorldService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.springframework.stereotype.Component

@Component
class CustomItemDropListener(
    private val jobService: JobService,
    private val customItemService: CustomItemService,
    private val worldService: WorldService,
    private val taskScheduler: TaskScheduler,
) : Listener {

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if (worldService.getSrpWorld(event.block.world)?.resourceWorld != true) return
        val material = event.block.type

        val customItem = CustomMaterial.entries
            .find { it.dropSource is CustomMaterialBlockSource && (it.dropSource).material == material }
            ?.let { jobService.generateItem(event.player.uniqueId, it) }
            ?: return
        taskScheduler.runLater(1) {
            event.player.world.dropItemNaturally(
                event.block.location,
                customItemService.createCustomItem(CustomItemStack(customItem, 1))
            )
        }
    }

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        if (worldService.getSrpWorld(event.entity.world)?.resourceWorld != true) return
        val entity = event.entity
        val killer = entity.killer ?: return
        val customItem = CustomMaterial.entries
            .find { it.dropSource is CustomMaterialEntitySource && (it.dropSource).entityType == entity.type }
            ?.let { jobService.generateItem(killer.uniqueId, it) }
            ?: return
        event.drops.add(customItemService.createCustomItem(CustomItemStack(customItem, 1)))
    }
}


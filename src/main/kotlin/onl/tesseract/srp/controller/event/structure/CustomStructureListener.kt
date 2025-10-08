package onl.tesseract.srp.controller.event.structure

import dev.lone.itemsadder.api.CustomFurniture
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

abstract class CustomStructureListener(val structureName: String) : Listener {

    abstract protected fun onClick(player: Player);

    @EventHandler
    fun onPlayerInteract(e: PlayerInteractEvent){
        val block = e.clickedBlock ?: return
        val item = CustomFurniture.byAlreadySpawned(block)?:return
        if (e.hand != EquipmentSlot.HAND) return

        println(item.namespacedID)
        if (item.namespacedID == structureName){
            e.isCancelled = true
            onClick(e.player)
        }
    }
}
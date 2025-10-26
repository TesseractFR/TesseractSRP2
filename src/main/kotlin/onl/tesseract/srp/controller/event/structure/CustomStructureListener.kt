package onl.tesseract.srp.controller.event.structure

import dev.lone.itemsadder.api.CustomFurniture
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

abstract class CustomStructureListener() : Listener {

    abstract protected fun onClick(player: Player, furniture1: CustomFurniture) : Boolean;

    @EventHandler
    fun onPlayerInteract(e: PlayerInteractEvent){
        val block = e.clickedBlock ?: return
        val furniture = CustomFurniture.byAlreadySpawned(block)?:return
        if (e.hand != EquipmentSlot.HAND || e.action != Action.RIGHT_CLICK_BLOCK) return
        e.isCancelled = onClick(e.player,furniture)

    }
}
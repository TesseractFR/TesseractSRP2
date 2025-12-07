package onl.tesseract.srp.controller.event.equipment.annexionStick

import onl.tesseract.lib.equipment.EquipmentService
import onl.tesseract.srp.util.equipment.annexionStick.AnnexionStickInvocable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
class AnnexionStickUseListener(
    private val equipmentService: EquipmentService
) : Listener {

    @EventHandler(ignoreCancelled = true)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND || event.item == null) return
        val equipment = equipmentService.getEquipment(event.player.uniqueId)
        val invocable = equipment.invocables
            .filterIsInstance<AnnexionStickInvocable>()
            .firstOrNull { invocable ->
                invocable.isInvoked &&
                        invocable.material == event.item!!.type &&
                        event.player.inventory.heldItemSlot == invocable.handSlot
            }
        invocable?.use(event)
    }
}


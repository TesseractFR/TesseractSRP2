package onl.tesseract.srp.controller.event.equipment.elytra

import onl.tesseract.lib.equipment.EquipmentService
import onl.tesseract.lib.event.equipment.invocable.Elytra
import onl.tesseract.srp.domain.equipment.elytra.event.ElytraAutoGlideToggleRequestedEvent
import org.bukkit.event.Listener
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
class ElytraAutoGlideToggleListener(
    private val equipmentService: EquipmentService
) : Listener {

    @EventListener
    fun onToggle(event: ElytraAutoGlideToggleRequestedEvent) {
        val equipment = equipmentService.getEquipment(event.playerId)
        val elytra = equipment.get(Elytra::class.java) ?: return
        elytra.toggleAutoGlideEnabled(!elytra.autoGlide)
    }
}

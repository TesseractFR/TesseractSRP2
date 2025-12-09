package onl.tesseract.srp.controller.event.equipment.elytra

import onl.tesseract.lib.equipment.EquipmentService
import onl.tesseract.lib.event.equipment.invocable.Elytra
import onl.tesseract.lib.util.ChatFormats.ELYTRA_ERROR
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.domain.equipment.elytra.event.ElytraToggleRequestedEvent
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
class ElytraToggleInvocationListener(
    private val equipmentService: EquipmentService
) : Listener {

    @EventListener
    fun onToggleRequested(event: ElytraToggleRequestedEvent) {
        val player = Bukkit.getPlayer(event.playerId) ?: return
        val equipment = equipmentService.getEquipment(event.playerId)
        val elytra = equipment.get(Elytra::class.java) ?: run {
            player.sendMessage(ELYTRA_ERROR + "Tu ne possèdes pas d'élytra personnalisée.")
            return
        }
        if (elytra.isInvoked) {
            equipmentService.uninvoke(player, elytra)
        } else {
            if (!elytra.canInvoke(player)) {
                player.sendMessage(ELYTRA_ERROR + "Vous devez libérer votre plastron pour invoquer vos ailes.")
                return
            }
            equipmentService.invoke(player, Elytra::class.java, null, true)
        }
    }
}
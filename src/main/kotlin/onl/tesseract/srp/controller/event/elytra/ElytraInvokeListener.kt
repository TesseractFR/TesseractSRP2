package onl.tesseract.srp.controller.event.elytra

import onl.tesseract.lib.equipment.EquipmentService
import onl.tesseract.lib.event.equipment.invocable.Elytra
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.springframework.stereotype.Component

@Component
class ElytraInvokeListener(
    private val equipmentService: EquipmentService
) : Listener {

    @EventHandler
    fun onFirstJoin(event: PlayerJoinEvent) {

        val equipment = equipmentService.getEquipment(event.player.uniqueId)
        val elytra = equipment.get(Elytra::class.java)

        if (elytra == null) {
            val newElytra = Elytra(event.player.uniqueId, false, 0)
            equipmentService.add(event.player.uniqueId, newElytra)
            equipmentService.invoke(event.player, Elytra::class.java)
            event.player.sendMessage("§aTu as reçu les Ailes Célestes !")
        }
    }
}

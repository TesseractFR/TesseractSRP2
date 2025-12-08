package onl.tesseract.srp.controller.event.equipment.elytra

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.lib.equipment.EquipmentService
import onl.tesseract.lib.event.equipment.invocable.Elytra
import onl.tesseract.srp.domain.equipment.elytra.event.ElytraPropulsionRequestedEvent
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
class ElytraPropulsionListener(
    private val equipmentService: EquipmentService
) : Listener {

    @EventListener
    fun onPropulsion(event: ElytraPropulsionRequestedEvent) {
        val player = Bukkit.getPlayer(event.playerId) ?: return
        val equipment = equipmentService.getEquipment(event.playerId)
        val elytra = equipment.get(Elytra::class.java) ?: return
        if (!elytra.isInvoked) {
            player.sendMessage(
                Component.text(
                    "Vous devez invoquer vos ailes pour utiliser cette fonction.",
                    NamedTextColor.RED
                )
            )
            return
        }
        elytra.synergicPropulsion(player)
    }
}

package onl.tesseract.srp.controller.event.equipment.elytra

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.lib.equipment.EquipmentMenu
import onl.tesseract.lib.equipment.EquipmentService
import onl.tesseract.lib.event.equipment.invocable.Elytra
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.domain.equipment.elytra.event.ElytraGivenEvent
import onl.tesseract.srp.util.ElytraChatFormat
import org.bukkit.Bukkit
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
class ElytraGivenListener(
    private val equipmentService: EquipmentService
) {
    @EventListener
    fun onElytraGiven(event: ElytraGivenEvent) {
        val player = Bukkit.getPlayer(event.playerId) ?: return
        val chestFree = player.inventory.chestplate == null
        if (chestFree) {
            equipmentService.invoke(player, Elytra::class.java)
            player.sendMessage(
                ElytraChatFormat + Component.text("Tu as reçu tes Ailes Célestes !", NamedTextColor.AQUA))
        } else {
            val menu = EquipmentMenu(player, equipmentService)
            menu.open(player)
            player.sendMessage(
                ElytraChatFormat + Component.text(
                    "Ton plastron est occupé, choisis un emplacement dans le menu d'équipement.",
                    NamedTextColor.YELLOW
                )
            )
        }
    }
}
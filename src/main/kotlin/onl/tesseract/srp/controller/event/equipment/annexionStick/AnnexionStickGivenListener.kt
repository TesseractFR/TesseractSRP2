package onl.tesseract.srp.controller.event.equipment.annexionStick

import net.kyori.adventure.text.Component
import onl.tesseract.lib.equipment.EquipmentMenu
import onl.tesseract.lib.equipment.EquipmentService
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.domain.equipment.annexionStick.event.AnnexionStickGivenEvent
import onl.tesseract.srp.util.CampementChatFormat
import onl.tesseract.srp.util.GuildChatFormat
import onl.tesseract.srp.util.equipment.annexionStick.AnnexionStickInvocable
import onl.tesseract.srp.util.equipment.annexionStick.CampementAnnexionStickInvocable
import onl.tesseract.srp.util.equipment.annexionStick.GuildAnnexionStickInvocable
import org.bukkit.Bukkit
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
class AnnexionStickGivenListener(
    private val equipmentService: EquipmentService
) {

    @EventListener
    fun onAnnexionStickGiven(event: AnnexionStickGivenEvent) {
        val player = Bukkit.getPlayer(event.playerId) ?: return
        val equipment = equipmentService.getEquipment(event.playerId)
        val invocable = equipment.get(event.invocableType) ?: return
        val (chatPrefix, stickLabel) = resolveContext(invocable) ?: return
        val hasFreeSlotInHotbar = (0..8).any { slot ->
            player.inventory.getItem(slot) == null
        }
        if (hasFreeSlotInHotbar) {
            equipmentService.invoke(player, event.invocableType)
            player.sendMessage(chatPrefix + "Tu as reçu un $stickLabel.")
        } else {
            val menu = EquipmentMenu(player, equipmentService)
            menu.mainHandInvocationMenu(invocable, player)
            player.sendMessage(chatPrefix + "Ton inventaire est plein, choisis un emplacement dans le menu d'équipement.")
        }
    }

    private fun resolveContext(invocable: AnnexionStickInvocable): Pair<Component, String>? =
        when (invocable) {
            is CampementAnnexionStickInvocable -> CampementChatFormat to "Bâton d'Annexion de campement"
            is GuildAnnexionStickInvocable -> GuildChatFormat to "Bâton d'Annexion de guilde"
            else -> null
        }
}

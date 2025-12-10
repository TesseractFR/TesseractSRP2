package onl.tesseract.srp.controller.event.equipment.elytra

import onl.tesseract.lib.equipment.EquipmentService
import onl.tesseract.lib.event.equipment.invocable.Elytra
import onl.tesseract.lib.util.ChatFormats.ELYTRA_ERROR
import onl.tesseract.lib.util.ChatFormats.ELYTRA_SUCCESS
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.domain.equipment.elytra.event.ElytraToggleRequestedEvent
import onl.tesseract.srp.service.equipment.elytra.ElytraInvocationResult
import onl.tesseract.srp.service.equipment.elytra.ElytraService
import onl.tesseract.srp.util.PlayerUtils.tryFreeChestplateSlot
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
class ElytraInvocationListener(
    private val equipmentService: EquipmentService,
    private val elytraService: ElytraService
) : Listener {

    @EventListener
    fun onToggleRequested(event: ElytraToggleRequestedEvent) {
        val player = Bukkit.getPlayer(event.playerId) ?: return
        val result = elytraService.getInvocationResult(event.playerId)
        when (result) {
            ElytraInvocationResult.NO_ELYTRA -> {
                if (!canEquipElytra(player)) return
                elytraService.createElytra(player.uniqueId)
                invokeElytra(player)
            }
            ElytraInvocationResult.ALREADY_INVOKED -> {
                val equipment = equipmentService.getEquipment(event.playerId)
                val elytra = equipment.get(Elytra::class.java) ?: return
                equipmentService.uninvoke(player, elytra)
            }
            ElytraInvocationResult.READY_TO_INVOKE -> {
                if (!canEquipElytra(player)) return
                invokeElytra(player)
            }
        }
    }

    private fun canEquipElytra(player: Player): Boolean {
        if (!tryFreeChestplateSlot(player)) {
            player.closeInventory()
            player.sendMessage(
                ELYTRA_ERROR + "Ton inventaire est plein, impossible d'invoquer tes Ailes Célestes."
            )
            return false
        }
        return true
    }

    private fun invokeElytra(player: Player) {
        equipmentService.invoke(player, Elytra::class.java, null, true)
        player.sendMessage(ELYTRA_SUCCESS + "Tu as invoqué tes Ailes Célestes !")
    }

}



package onl.tesseract.srp.service.equipment.annexionStick

import onl.tesseract.lib.equipment.EquipmentMenu
import onl.tesseract.lib.equipment.EquipmentService
import onl.tesseract.srp.util.equipment.annexionStick.AnnexionStickGiveResult
import onl.tesseract.srp.util.equipment.annexionStick.AnnexionStickInvocable
import org.bukkit.entity.Player
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class AnnexionStickService(
    private val equipmentService: EquipmentService
) {

    fun <T : AnnexionStickInvocable> giveStick(
        player: Player,
        invocableType: KClass<T>,
        factory: (playerId: java.util.UUID) -> T
    ): AnnexionStickGiveResult {
        val equipment = equipmentService.getEquipment(player.uniqueId)
        val invocable = equipment.get(invocableType.java) ?: factory(player.uniqueId).also {
            equipmentService.add(player.uniqueId, it)
        }
        val hasFreeSlotInHotbar = (0..8).any { player.inventory.getItem(it) == null }
        return if (hasFreeSlotInHotbar) {
            equipmentService.invoke(player, invocableType.java)
            AnnexionStickGiveResult.SUCCESS
        } else {
            val menu = EquipmentMenu(player, equipmentService)
            menu.mainHandInvocationMenu(invocable, player)
            AnnexionStickGiveResult.OPENED_MENU
        }
    }
}

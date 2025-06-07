package onl.tesseract.srp.service.elytra

import onl.tesseract.lib.equipment.EquipmentService
import onl.tesseract.lib.event.equipment.invocable.Elytra
import onl.tesseract.lib.event.equipment.invocable.EnumElytraUpgrade
import onl.tesseract.srp.service.player.SrpPlayerService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

private const val BASE_PRICE = 100
private const val MAX_LEVEL = 9

@Service
open class ElytraService(
    private val srpPlayerService: SrpPlayerService,
    private val equipmentService: EquipmentService
) {
    @Transactional
    open fun buyNextElytraUpgrade(playerID: UUID, elytra: Elytra, upgrade: EnumElytraUpgrade): Boolean {
        val currentLevel = elytra.getLevel(upgrade)
        val price = getPriceForLevel(currentLevel)
        val player = srpPlayerService.getPlayer(playerID)
        var success = false

        if (price != null && player.illuminationPoints >= price) {
            player.addIlluminationPoints(-price)
            elytra.upgradeLevel(upgrade)

            if (upgrade == EnumElytraUpgrade.SPEED) {
                elytra.enableSpeedUpgrade()
            }
            equipmentService.saveEquipment(equipmentService.getEquipment(playerID))
            srpPlayerService.savePlayer(player)
            success = true
        }
        return success
    }

    fun getPriceForLevel(level: Int): Int? =
        if (level in 0 until MAX_LEVEL) BASE_PRICE * (level + 1) else null

    fun getMaxLevel(): Int = MAX_LEVEL
}

package onl.tesseract.srp.service.elytra

import onl.tesseract.lib.event.equipment.invocable.Elytra
import onl.tesseract.srp.domain.elytra.EnumElytraUpgrade
import org.springframework.stereotype.Service

@Service
class ElytraUpgradeService {
    private val prices = listOf(100, 200, 300, 400, 500, 600, 700, 800, 900, 1000)

    fun getLevel(elytra: Elytra, upgrade: EnumElytraUpgrade): Int {
        return when (upgrade) {
            EnumElytraUpgrade.PROTECTION -> elytra.protectionLevel
            EnumElytraUpgrade.SPEED -> elytra.speedLevel
        }
    }

    fun setLevel(elytra: Elytra, upgrade: EnumElytraUpgrade, level: Int) {
        when (upgrade) {
            EnumElytraUpgrade.PROTECTION -> elytra.protectionLevel = level
            EnumElytraUpgrade.SPEED -> elytra.speedLevel = level
        }
        elytra.refreshItemInInventory()
    }

    fun upgradeLevel(elytra: Elytra, upgrade: EnumElytraUpgrade) {
        when (upgrade) {
            EnumElytraUpgrade.PROTECTION -> elytra.protectionLevel++
            EnumElytraUpgrade.SPEED -> elytra.speedLevel++
        }
        elytra.refreshItemInInventory()
    }

    fun enableSpeedUpgrade(elytra: Elytra) {
        elytra.ignoreSpeedLevel = false
    }

    fun getPriceForLevel(level: Int): Int? = prices.getOrNull(level)
    fun getMaxLevel(): Int = prices.size


}

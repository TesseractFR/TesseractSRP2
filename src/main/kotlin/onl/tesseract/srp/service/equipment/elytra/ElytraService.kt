package onl.tesseract.srp.service.equipment.elytra

import onl.tesseract.lib.equipment.EquipmentService
import onl.tesseract.lib.event.equipment.invocable.Elytra
import onl.tesseract.lib.event.equipment.invocable.ElytraUpgrade
import onl.tesseract.srp.DomainEventPublisher
import onl.tesseract.srp.domain.equipment.elytra.event.ElytraAutoGlideToggleRequestedEvent
import onl.tesseract.srp.domain.equipment.elytra.event.ElytraPropulsionRequestedEvent
import onl.tesseract.srp.domain.equipment.elytra.event.ElytraToggleRequestedEvent
import onl.tesseract.srp.service.player.SrpPlayerService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

private const val BASE_PRICE = 100
private const val MAX_LEVEL = 9 // (ingame lvl 10)

private const val SPEED_MULTIPLIER = 0.10
private const val PROTECTION_MULTIPLIER = 0.5
private const val PERCENT_CONVERSION = 100
private const val MS_TO_SECONDS = 1000

@Service
open class ElytraService(
    private val srpPlayerService: SrpPlayerService,
    private val eventPublisher: DomainEventPublisher,
    private val equipmentService: EquipmentService
) {

    @Transactional
    open fun createElytra(playerId: UUID) {
        val equipment = equipmentService.getEquipment(playerId)
        val existing = equipment.get(Elytra::class.java)
        if (existing != null) {
            return
        }
        val elytra = Elytra(
            playerUUID = playerId,
            invoked = false,
            handSlot = -1,
        )
        equipmentService.add(playerId, elytra)
    }

    @Transactional
    open fun toggleInvocation(playerId: UUID) {
        eventPublisher.publish(ElytraToggleRequestedEvent(playerId))
    }

    @Transactional
    open fun getInvocationResult(playerId: UUID): ElytraInvocationResult {
        val equipment = equipmentService.getEquipment(playerId)
        val elytra = equipment.get(Elytra::class.java) ?: return ElytraInvocationResult.NO_ELYTRA
        return if (elytra.isInvoked) {
            ElytraInvocationResult.ALREADY_INVOKED
        } else {
            ElytraInvocationResult.READY_TO_INVOKE
        }
    }

    @Transactional
    open fun toggleAutoGlide(playerId: UUID) {
        eventPublisher.publish(ElytraAutoGlideToggleRequestedEvent(playerId))
    }

    @Transactional
    open fun requestPropulsion(playerId: UUID) {
        eventPublisher.publish(ElytraPropulsionRequestedEvent(playerId))
    }

    @Transactional
    open fun getMenuState(playerId: UUID): ElytraMenuState {
        val equipment = equipmentService.getEquipment(playerId)
        val elytra = equipment.get(Elytra::class.java) ?: return ElytraMenuState(hasElytra = false)
        return ElytraMenuState(
            hasElytra = true,
            isInvoked = elytra.isInvoked,
            autoGlide = elytra.autoGlide
        )
    }

    @Transactional
    open fun getUpgradeMenuState(playerId: UUID): ElytraUpgradeMenuState {
        val equipment = equipmentService.getEquipment(playerId)
        val elytra = equipment.get(Elytra::class.java)
        val player = srpPlayerService.getPlayer(playerId)
        val entries = ElytraUpgrade.entries.map { upgrade ->
            val currentLevel = elytra?.getLevel(upgrade) ?: 0
            val nextLevel = if (currentLevel < MAX_LEVEL) currentLevel + 1 else null
            val price = if (currentLevel < MAX_LEVEL) getPriceForLevel(currentLevel) else null
            val canAfford = price != null && player.illuminationPoints >= price
            ElytraUpgradeEntry(
                upgrade = upgrade,
                currentLevel = currentLevel,
                nextLevel = nextLevel,
                maxLevel = MAX_LEVEL,
                price = price,
                canAfford = canAfford
            )
        }
        return ElytraUpgradeMenuState(
            hasElytra = true,
            money = player.money,
            illuminationPoints = player.illuminationPoints,
            rankLabel = player.rank.toString(),
            entries = entries
        )
    }

    @Transactional
    open fun tryBuyNextUpgrade(
        playerId: UUID,
        upgrade: ElytraUpgrade
    ): ElytraUpgradeResult {
        val equipment = equipmentService.getEquipment(playerId)
        val elytra = equipment.get(Elytra::class.java) ?: return ElytraUpgradeResult.NO_ELYTRA
        val currentLevel = elytra.getLevel(upgrade)
        if (currentLevel >= MAX_LEVEL) {
            return ElytraUpgradeResult.MAX_LEVEL_REACHED
        }
        val price = getPriceForLevel(currentLevel) ?: return ElytraUpgradeResult.MAX_LEVEL_REACHED
        val success = srpPlayerService.giveIlluminationPoints(playerId, -price)
        if (!success) {
            return ElytraUpgradeResult.NOT_ENOUGH_POINTS
        }
        elytra.upgradeLevel(upgrade)
        if (upgrade == ElytraUpgrade.SPEED) {
            elytra.enableSpeedUpgrade()
        }
        equipmentService.saveEquipment(equipment)
        return ElytraUpgradeResult.SUCCESS
    }

    @Transactional
    open fun setUpgradeLevel(playerId: UUID, upgrade: ElytraUpgrade, level: Int): Boolean {
        val equipment = equipmentService.getEquipment(playerId)
        val elytra = equipment.get(Elytra::class.java) ?: return false
        elytra.setLevel(upgrade, level)
        equipmentService.saveEquipment(equipment)
        return true
    }

    fun getUpgradeStats(upgrade: ElytraUpgrade, level: Int): ElytraUpgradeStats {
        return when (upgrade) {
            ElytraUpgrade.SPEED -> {
                val current = SPEED_MULTIPLIER * (level + 1) * PERCENT_CONVERSION
                val next = SPEED_MULTIPLIER * (level + 2) * PERCENT_CONVERSION
                ElytraUpgradeStats(current, next, "%", upgrade)
            }
            ElytraUpgrade.PROTECTION -> {
                val current = PROTECTION_MULTIPLIER * level
                val next = PROTECTION_MULTIPLIER * (level + 1)
                ElytraUpgradeStats(current, next, "points", upgrade)
            }
            ElytraUpgrade.BOOST_NUMBER -> {
                val current = Elytra.getBoostCount(level).toDouble()
                val next = Elytra.getBoostCount(level + 1).toDouble()
                ElytraUpgradeStats(current, next, "boosts", upgrade)
            }
            ElytraUpgrade.RECOVERY -> {
                val current = Elytra.getBaseRecoveryTime(level) / MS_TO_SECONDS
                val next = Elytra.getBaseRecoveryTime(level + 1) / MS_TO_SECONDS
                ElytraUpgradeStats(current.toDouble(), next.toDouble(), "s", upgrade)
            }
        }
    }

    private fun getPriceForLevel(level: Int): Int? =
        if (level in 0 until MAX_LEVEL) BASE_PRICE * (level + 1) else null

}

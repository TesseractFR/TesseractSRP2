package onl.tesseract.srp.controller.event.elytra

import onl.tesseract.lib.equipment.EquipmentService
import onl.tesseract.srp.domain.elytra.ElytraInvocable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.springframework.stereotype.Component

@Component
class ElytraSneakBoostListener(
    private val equipmentService: EquipmentService
) : Listener {

    companion object {
        private const val BOOST_POWER = 1.2 // A modifier avec les améliorations
        private const val SPEED_THRESHOLD = 1.5
    }

    @EventHandler
    fun onSneak(event: PlayerToggleSneakEvent) {
        val player = event.player

        val equipment = equipmentService.getEquipment(player.uniqueId)
        val invocable = equipment.get(ElytraInvocable::class.java)

        val shouldBoost = event.isSneaking
                && player.isGliding
                && invocable != null
                && invocable.isInvoked
                && player.velocity.length() <= SPEED_THRESHOLD

        if (!shouldBoost) return

        val direction = player.location.direction.normalize().multiply(BOOST_POWER)
        player.velocity = direction
    }
}

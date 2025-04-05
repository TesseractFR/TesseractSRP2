package onl.tesseract.srp.service

import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.lib.task.TaskScheduler
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.service.player.SrpPlayerService
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.springframework.stereotype.Service

@Service
class TeleportationService(private val playerService: SrpPlayerService, private val taskService: TaskScheduler) {

    fun teleport(player: Player, to: Location, callback: (() -> Unit)? = null) {
        val srpPlayer = playerService.getPlayer(player.uniqueId)
        teleport(player, to, srpPlayer.rank.tpDelay, callback)
    }

    fun teleport(player: Player, to: Location, duration: Int, callback: (() -> Unit)? = null) {
        player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, duration + 20, 1))
        player.addPotionEffect(PotionEffect(PotionEffectType.NAUSEA, (duration * 1.75f).toInt(), 1))
        player.playSound(player.location, Sound.BLOCK_PORTAL_TRIGGER, 1f, 1f)

        val startLocation = player.location
        var currentDuration = 0
        taskService.runTimer(0, 10, 0) { task ->
            currentDuration += 10
            if (currentDuration >= duration) {
                // Teleport
                task.cancel()
                player.teleport(to)
                callback?.invoke()
            } else {
                // Check if player has moved
                if (!player.isOnline) return@runTimer task.cancel()
                if (player.location.block != startLocation.block) {
                    task.cancel()
                    player.removePotionEffect(PotionEffectType.BLINDNESS)
                    player.sendMessage(NamedTextColor.RED + "Téléportation annulée.")
                }
            }
        }
    }
}
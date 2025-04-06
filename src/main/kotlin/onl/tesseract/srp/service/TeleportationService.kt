package onl.tesseract.srp.service

import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.core.boutique.BoutiqueService
import onl.tesseract.core.cosmetics.TeleportationAnimation
import onl.tesseract.lib.logger.LoggerFactory
import onl.tesseract.lib.task.TaskScheduler
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.service.player.SrpPlayerService
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.slf4j.Logger
import org.springframework.stereotype.Service

private val logger: Logger = LoggerFactory.getLogger(TeleportationService::class.java)

/**
 * Teleport players to different locations, with a teleportation delay depending on their rank, and a teleportation
 * animation.
 */
@Service
class TeleportationService(
    private val playerService: SrpPlayerService,
    private val taskService: TaskScheduler,
    private val plugin: Plugin,
    private val boutiqueService: BoutiqueService,
) {

    /**
     * Teleport the player to the given location, with a delay depending on his rank, and the active teleportation
     * animation
     * @param callback Callback called once the teleportation happened
     */
    fun teleport(player: Player, to: Location, callback: (() -> Unit)? = null) {
        val srpPlayer = playerService.getPlayer(player.uniqueId)
        val playerBoutiqueInfo = boutiqueService.getPlayerBoutiqueInfo(player.uniqueId)
        teleport(player, to, srpPlayer.rank.tpDelay, playerBoutiqueInfo.activeTpAnimation, callback)
    }

    /**
     * Teleport a player with a duration and a teleportation animation
     * @param player Player to teleport
     * @param to Location of destination
     * @param duration Delay of the teleportation in ticks
     * @param animation Animation to display at the player location, and at destination of the destination chunk is loaded
     * @param callback Callback called once the teleportation happened
     */
    fun teleport(
        player: Player,
        to: Location,
        duration: Int,
        animation: TeleportationAnimation,
        callback: (() -> Unit)? = null
    ) {
        logger.debug("Teleporting player {} from {} to {}", player.name, player.location, to)
        player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, duration + 20, 1))
        player.addPotionEffect(PotionEffect(PotionEffectType.NAUSEA, (duration * 1.75f).toInt(), 1))
        player.playSound(player.location, Sound.BLOCK_PORTAL_TRIGGER, 1f, 1f)
        animation.animate(plugin, player.location, duration.toDouble())
        if (to.chunk.isLoaded)
            animation.animate(plugin, to, duration.toDouble())

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
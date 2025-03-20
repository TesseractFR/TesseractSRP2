package onl.tesseract.srp.controller.event.job

import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.service.job.PlayerLevelUpEvent
import onl.tesseract.srp.util.jobsChatFormat
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.springframework.stereotype.Component

@Component
class JobsEventListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onLevelUp(event: PlayerLevelUpEvent) {
        Bukkit.getPlayer(event.playerID)?.let { player ->
            player.sendMessage(jobsChatFormat + "Tu es passé niveau " + (NamedTextColor.GREEN + "${event.level}"))
            player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
        }
    }
}
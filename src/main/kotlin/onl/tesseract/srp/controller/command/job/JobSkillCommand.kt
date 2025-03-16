package onl.tesseract.srp.controller.command.job

import onl.tesseract.commandBuilder.CommandContext
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.CommandBody
import onl.tesseract.srp.PLUGIN_INSTANCE
import onl.tesseract.srp.controller.menu.job.JobSkillJobSelectionMenu
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.scheduler.BukkitRunnable
import org.springframework.stereotype.Component

@Component
@Command(name = "jobskill", description = "Ouvrir le menu des compétences de métier", playerOnly = true)
class JobSkillCommand : CommandContext() {

    @CommandBody
    fun body(player: Player) {
        JobSkillJobSelectionMenu(player.uniqueId).open(player)
    }
}

@Component
class Aled : Listener {

    @EventHandler
    fun aled(event: InventoryCloseEvent) {
        val player = event.player
        object : BukkitRunnable() {
            override fun run() {
                (player as Player).updateInventory()
            }
        }.runTaskLater(PLUGIN_INSTANCE, 1L)
    }

}
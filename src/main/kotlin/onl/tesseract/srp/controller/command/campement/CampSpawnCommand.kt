package onl.tesseract.srp.controller.command.campement

import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.CommandBody
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component

/**
 * Teleport to the player's camp.
 */
@Component
@Command(name = "spawn", playerOnly = true)
class CampSpawnCommand(private val campementService: CampementService) {

    @CommandBody
    fun onCommand(sender: Player) {
        val playerID = sender.uniqueId
        val campement = campementService.getCampementByOwner(playerID)
        if (campement == null) {
            sender.sendMessage("§cTu ne possèdes pas de campement !")
            return
        }
        val spawnLocation = campement.spawnLocation
        sender.teleport(spawnLocation)
        sender.sendMessage("§aTu as été téléporté à ton campement !")
    }
}

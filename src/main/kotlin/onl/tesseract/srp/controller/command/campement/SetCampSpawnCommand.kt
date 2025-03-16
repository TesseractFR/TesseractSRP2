package onl.tesseract.srp.controller.command.campement

import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.CommandBody
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component

@Component
@Command(name = "setspawn", playerOnly = true)
class SetCampSpawnCommand(private val campementService: CampementService) {

    @CommandBody
    fun onCommand(sender: Player) {
        val playerID = sender.uniqueId
        val campement = campementService.getCampementByOwner(playerID)

        if (campement == null) {
            sender.sendMessage("§cTu ne possèdes pas de campement !")
            return
        }

        val location = sender.location
        campementService.setSpawnpoint(playerID, location)
        sender.sendMessage("§aNouveau spawn défini ici !")
    }
}

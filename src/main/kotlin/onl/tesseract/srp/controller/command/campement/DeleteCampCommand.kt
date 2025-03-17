package onl.tesseract.srp.controller.command.campement

import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.CommandBody
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component

/**
 * Delete a camp
 */
@Component
@Command(name = "delete", playerOnly = true)
class DeleteCampCommand(private val campementService: CampementService) {

    @CommandBody
    fun onCommand(sender: Player) {
        val playerID = sender.uniqueId

        val existingCampement = campementService.getCampementByOwner(playerID)
        if (existingCampement == null) {
            sender.sendMessage("§cTu ne possèdes pas de campement à supprimer !")
            return
        }
        campementService.deleteCampement(existingCampement.id)
        sender.sendMessage("§aTon campement a été supprimé avec succès.")
    }
}

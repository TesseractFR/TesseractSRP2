package onl.tesseract.srp.controller.command.campement

import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.CommandBody
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component

/**
 * Command to create a camp by claiming the chunk the player is standing on.
 */
@Component
@Command(name = "create", playerOnly = true)
class CreateCampCommand(private val campementService: CampementService) {

    @CommandBody
    fun onCommand(sender: Player) {
        val playerID = sender.uniqueId
        val location = sender.location
        val chunk = sender.location.chunk
        val chunkCoord = "${chunk.x},${chunk.z}"

        val existingCampement = campementService.getCampementByOwner(playerID)
        if (existingCampement != null) {
            sender.sendMessage("§cTu possèdes déjà un campement !")
            return
        }

        val success = campementService.createCampement(playerID, listOf(chunkCoord), location)
        if (success) {
            sender.sendMessage("§aCampement créé avec succès ! Tu contrôles maintenant ce chunk (${chunkCoord}).")
        } else {
            sender.sendMessage("§cImpossible de créer le campement ici, ce chunk appartient déjà à un autre campement.")
        }
    }
}

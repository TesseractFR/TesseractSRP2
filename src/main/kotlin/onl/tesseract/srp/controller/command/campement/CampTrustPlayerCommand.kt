package onl.tesseract.srp.controller.command.campement

import onl.tesseract.commandBuilder.annotation.*
import onl.tesseract.lib.command.argument.PlayerArg
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component

/**
 * Command to trust someone in your camp.
 */
@Component
@Command(
    name = "trust",
    args = [Argument(value = "joueur", clazz = PlayerArg::class)],
    playerOnly = true)

class CampTrustCommand(private val campementService: CampementService) {

    @CommandBody
    fun onCommand(sender: Player, @Env(key = "joueur") targetPlayer: Player) {
        val ownerID = sender.uniqueId
        val trustedPlayerID = targetPlayer.uniqueId
        val campement = campementService.getCampementByOwner(ownerID)
        if (campement == null) {
            sender.sendMessage("§cTu ne possèdes pas de campement !")
            return
        }
        if (ownerID == trustedPlayerID) {
            sender.sendMessage("§eC'est bien, tu as confiance en toi ! Mais bon, t'es déjà propriétaire :)")
            return
        }
        val success = campementService.trustPlayer(ownerID, trustedPlayerID)
        if (success) {
            sender.sendMessage("§a${targetPlayer.name} a été ajouté en tant que joueur de confiance dans ton campement !")
            targetPlayer.sendMessage("§aTu as été ajouté en tant que joueur de confiance dans le campement de ${sender.name}.")
        } else {
            sender.sendMessage("§cImpossible d'ajouter ce joueur. Assure-toi d'être le propriétaire du campement et que le joueur n'est pas déjà ajouté.")
        }
    }
}

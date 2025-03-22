package onl.tesseract.srp.controller.command.campement

import net.kyori.adventure.text.Component
import onl.tesseract.commandBuilder.annotation.*
import onl.tesseract.lib.command.argument.PlayerArg
import onl.tesseract.lib.util.ChatFormats
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
@Command(
    name = "trust",
    args = [Argument(value = "joueur", clazz = PlayerArg::class)],
    playerOnly = true
)
class CampTrustCommand(private val campementService: CampementService) {

    @CommandBody
    fun onCommand(sender: Player, @Env(key = "joueur") targetPlayer: Player) {
        val ownerID = sender.uniqueId
        val trustedPlayerID = targetPlayer.uniqueId
        val campement = campementService.getCampementOrWarn(sender) ?: return

        if (ownerID == trustedPlayerID) {
            sender.sendMessage(
                ChatFormats.CHAT.append(
                    Component.text("C'est bien, tu as confiance en toi ! Mais bon, t'es déjà propriétaire :)")
                )
            )
            return
        }

        val success = campementService.trustPlayer(ownerID, trustedPlayerID)
        if (success) {
            sender.sendMessage(
                ChatFormats.CHAT_SUCCESS.append(
                    Component.text("${targetPlayer.name} a été ajouté en tant que joueur de confiance dans ton campement !")
                )
            )
            targetPlayer.sendMessage(
                ChatFormats.CHAT_SUCCESS.append(
                    Component.text("Tu as été ajouté en tant que joueur de confiance dans le campement de ${sender.name}.")
                )
            )
        } else {
            sender.sendMessage(
                ChatFormats.CHAT_ERROR.append(
                    Component.text("Impossible d'ajouter ce joueur. Assure-toi d'être le propriétaire du campement et que le joueur n'est pas déjà ajouté.")
                )
            )
        }
    }
}

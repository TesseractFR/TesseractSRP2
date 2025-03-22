package onl.tesseract.srp.controller.command.campement

import net.kyori.adventure.text.Component
import onl.tesseract.srp.controller.command.argument.TrustedPlayerArg
import onl.tesseract.commandBuilder.annotation.*
import onl.tesseract.lib.util.ChatFormats
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
@Command(
    name = "untrust",
    args = [Argument(value = "joueur", clazz = TrustedPlayerArg::class)],
    playerOnly = true
)
class CampUntrustCommand(
    private val campementService: CampementService
) {

    @CommandBody
    fun onCommand(sender: Player, @Env(key = "joueur") targetName: String) {
        val ownerID = sender.uniqueId
        val campement = campementService.getCampementOrWarn(sender) ?: return

        val trustedPlayerUUID = campement.trustedPlayers.find {
            Bukkit.getOfflinePlayer(it).name == targetName
        }

        if (trustedPlayerUUID == null) {
            sender.sendMessage(
                ChatFormats.CHAT_ERROR.append(
                    Component.text("Ce joueur n'est pas dans ta liste de confiance.")
                )
            )
            return
        }

        val success = campementService.untrustPlayer(ownerID, trustedPlayerUUID)
        if (success) {
            sender.sendMessage(
                ChatFormats.CHAT_SUCCESS.append(
                    Component.text("$targetName a été retiré de la liste des joueurs de confiance de ton campement !")
                )
            )
        } else {
            sender.sendMessage(
                ChatFormats.CHAT_ERROR.append(
                    Component.text("Erreur lors du retrait de $targetName.")
                )
            )
        }
    }
}

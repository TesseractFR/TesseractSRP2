package onl.tesseract.srp.controller.command.staff.campement

import net.kyori.adventure.text.Component
import onl.tesseract.commandBuilder.annotation.*
import onl.tesseract.lib.util.ChatFormats
import onl.tesseract.srp.controller.command.argument.CampOwnerArg
import onl.tesseract.srp.controller.command.argument.TrustedPlayerArg
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
@Command(
    name = "untrust",
    args = [
        Argument("owner", clazz = CampOwnerArg::class),
        Argument("target", clazz = TrustedPlayerArg::class)
    ],
    permission = Perm("staff")
)
class CampUntrustStaffCommand(
    private val campementService: CampementService
) {
    @CommandBody
    fun onCommand(
        sender: CommandSender,
        @Env(key = "owner") ownerName: String,
        @Env(key = "target") targetName: String
    ) {
        val owner = Bukkit.getOfflinePlayer(ownerName)
        val ownerUUID = owner.uniqueId

        val campement = campementService.getCampementByOwner(ownerUUID)!!

        val trustedPlayerUUID = campement.trustedPlayers.find {
            Bukkit.getOfflinePlayer(it).name == targetName
        }

        if (!campement.trustedPlayers.contains(trustedPlayerUUID)) {
            sender.sendMessage(
                ChatFormats.CHAT_ERROR.append(
                    Component.text("$targetName n'est pas dans la liste des joueurs de confiance de $ownerName.")
                )
            )
            return
        }

        val success = trustedPlayerUUID?.let { campementService.untrustPlayer(ownerUUID, it) }
        if (success == true) {
            sender.sendMessage(
                ChatFormats.CHAT_SUCCESS.append(
                    Component.text("$targetName a été retiré de la liste des joueurs de confiance de $ownerName.")
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

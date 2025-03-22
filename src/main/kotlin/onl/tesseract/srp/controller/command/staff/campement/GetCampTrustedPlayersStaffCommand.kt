package onl.tesseract.srp.controller.command.staff.campement

import net.kyori.adventure.text.Component
import onl.tesseract.commandBuilder.annotation.*
import onl.tesseract.lib.util.ChatFormats
import onl.tesseract.srp.controller.command.argument.CampOwnerArg
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
@Command(
    name = "getTrustedPlayers",
    args = [Argument("owner", clazz = CampOwnerArg::class)],
    permission = Perm("staff")
)
class GetCampTrustedPlayersStaffCommand(
    private val campementService: CampementService
) {
    @CommandBody
    fun onCommand(
        sender: CommandSender,
        @Env(key = "owner") ownerName: String
    ) {
        val owner = Bukkit.getOfflinePlayer(ownerName)
        val campement = campementService.getCampementByOwner(owner.uniqueId)!!

        val trustedPlayers = campement.trustedPlayers
            .mapNotNull { Bukkit.getOfflinePlayer(it).name }
            .takeIf { it.isNotEmpty() }

        if (trustedPlayers == null) {
            sender.sendMessage(
                ChatFormats.CHAT.append(Component.text("Aucun joueur de confiance trouvé pour $ownerName."))
            )
        } else {
            val listText = trustedPlayers.joinToString(", ")
            sender.sendMessage(
                ChatFormats.CHAT_SUCCESS.append(
                    Component.text("Joueurs de confiance pour le campement de $ownerName : $listText")
                )
            )
        }
    }
}

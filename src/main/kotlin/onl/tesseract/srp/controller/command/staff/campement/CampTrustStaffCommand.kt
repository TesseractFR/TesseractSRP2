package onl.tesseract.srp.controller.command.staff.campement

import net.kyori.adventure.text.Component
import onl.tesseract.commandBuilder.annotation.*
import onl.tesseract.lib.command.argument.PlayerArg
import onl.tesseract.lib.util.ChatFormats
import onl.tesseract.srp.controller.command.argument.CampOwnerArg
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
@Command(
    name = "trust",
    args = [
        Argument("owner", clazz = CampOwnerArg::class),
        Argument("target", clazz = PlayerArg::class)
    ],
    permission = Perm("staff")
)
class CampTrustStaffCommand(
    private val campementService: CampementService
) {
    @CommandBody
    fun onCommand(
        sender: CommandSender,
        @Env(key = "owner") ownerName: String,
        @Env(key = "target") target: Player
    ) {
        val owner = Bukkit.getOfflinePlayer(ownerName)
        val ownerUUID = owner.uniqueId
        val targetUUID = target.uniqueId

        if (ownerUUID == targetUUID) {
            sender.sendMessage(
                ChatFormats.CHAT.append(
                    Component.text("Impossible d'ajouter le propriétaire lui-même en tant que joueur de confiance.")
                )
            )
            return
        }

        val success = campementService.trustPlayer(ownerUUID, targetUUID)
        if (success) {
            sender.sendMessage(
                ChatFormats.CHAT_SUCCESS.append(
                    Component.text("${target.name} a été ajouté en tant que joueur de confiance dans le campement de $ownerName.")
                )
            )
            target.sendMessage(
                ChatFormats.CHAT_SUCCESS.append(
                    Component.text("Tu as été ajouté en tant que joueur de confiance dans le campement de ${owner.name}.")
                )
            )
        } else {
            sender.sendMessage(
                ChatFormats.CHAT_ERROR.append(
                    Component.text("${target.name} est déjà dans la liste de confiance.")
                )
            )
        }
    }
}

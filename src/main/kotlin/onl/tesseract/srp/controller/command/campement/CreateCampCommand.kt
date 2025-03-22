package onl.tesseract.srp.controller.command.campement

import net.kyori.adventure.text.Component
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.CommandBody
import onl.tesseract.lib.util.ChatFormats
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component as SpringComponent


@SpringComponent
@Command(name = "create", playerOnly = true)
class CreateCampCommand(private val campementService: CampementService) {

    @CommandBody
    fun onCommand(sender: Player) {
        val playerID = sender.uniqueId
        val location = sender.location
        val chunk = sender.location.chunk
        val chunkCoord = "${chunk.x},${chunk.z}"

        if (campementService.getCampementByOwner(playerID) != null) {
            sender.sendMessage(ChatFormats.CHAT_ERROR.append(Component.text("Tu possèdes déjà un campement !")))
            return
        }

        val success = campementService.createCampement(playerID, listOf(chunkCoord), location)
        if (success) {
            sender.sendMessage(ChatFormats.CHAT_SUCCESS.append(Component.text("Campement créé avec succès ! Tu contrôles maintenant le chunk ($chunkCoord).")))
        } else {
            sender.sendMessage(ChatFormats.CHAT_ERROR.append(Component.text("Impossible de créer le campement ici, ce chunk appartient déjà à un autre campement.")))
        }
    }
}

package onl.tesseract.srp.controller.command.staff.campement

import net.kyori.adventure.text.Component
import onl.tesseract.commandBuilder.annotation.*
import onl.tesseract.lib.command.argument.PlayerArg
import onl.tesseract.lib.util.ChatFormats
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
@Command(
    name = "create",
    args = [Argument("joueur", clazz = PlayerArg::class)]
)
class CampCreateStaffCommand(private val campementService: CampementService) {

    @CommandBody
    fun onCommand(sender: Player, @Env(key = "joueur") target: Player) {
        val uuid = target.uniqueId
        val location = target.location
        // val location = sender.location (si on veut claim sur nous)
        val chunk = location.chunk
        val chunkKey = "${chunk.x},${chunk.z}"

        if (campementService.getCampementByOwner(uuid) != null) {
            sender.sendMessage(ChatFormats.CHAT_ERROR.append(Component.text("${target.name} possède déjà un campement.")))
            return
        }

        val success = campementService.createCampement(uuid, listOf(chunkKey), location)
        if (success) {
            sender.sendMessage(ChatFormats.CHAT_SUCCESS.append(Component.text("Campement créé pour ${target.name} dans le chunk $chunkKey.")))
            target.sendMessage(ChatFormats.CHAT_SUCCESS.append(Component.text("Un administrateur t'a créé un campement dans le chunk $chunkKey.")))
        } else {
            sender.sendMessage(ChatFormats.CHAT_ERROR.append(Component.text("Échec : le chunk $chunkKey est déjà revendiqué.")))
        }
    }
}


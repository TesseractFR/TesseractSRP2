package onl.tesseract.srp.controller.command.campement

import net.kyori.adventure.text.Component
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.CommandBody
import onl.tesseract.lib.util.ChatFormats
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component as SpringComponent


@SpringComponent
@Command(name = "setspawn", playerOnly = true)
class SetCampSpawnCommand(
    private val campementService: CampementService
) {

    @CommandBody
    fun onCommand(sender: Player) {
        val campement = campementService.getCampementOrWarn(sender) ?: return
        val location = sender.location
        campementService.setSpawnpoint(sender.uniqueId, location)
        sender.sendMessage(ChatFormats.CHAT_SUCCESS.append(Component.text("Nouveau point de spawn défini ici !")))
    }
}

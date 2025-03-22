package onl.tesseract.srp.controller.command.campement

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.CommandBody
import onl.tesseract.lib.util.ChatFormats
import onl.tesseract.srp.service.campement.CampementBorderRenderer
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
@Command(name = "border", playerOnly = true)
class CampBorderCommand(
    private val campementService: CampementService,
    private val borderRenderer: CampementBorderRenderer
) {

    @CommandBody
    fun onCommand(sender: Player) {
        val campement = campementService.getCampementOrWarn(sender) ?: return
        if (borderRenderer.isShowingBorders(sender)) {
            borderRenderer.clearBorders(sender)
            sender.sendMessage(
                ChatFormats.CHAT.append(
                    Component.text("Les bordures de ton campement ont été désactivées.")
                )
            )
        } else {
            borderRenderer.showBorders(sender, campement.listChunks.map { it.split(",").map(String::toInt) })
            sender.sendMessage(
                ChatFormats.CHAT_SUCCESS.append(
                    Component.text("Les bordures de ton campement sont maintenant visibles !")
                )
            )
        }
    }
}

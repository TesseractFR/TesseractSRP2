package onl.tesseract.srp.controller.command.campement

import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.CommandBody
import onl.tesseract.srp.service.campement.CampementBorderRenderer
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component

@Component
@Command(name = "border", playerOnly = true)
class CampBorderCommand(
    private val campementService: CampementService,
    private val borderRenderer: CampementBorderRenderer
) {

    @CommandBody
    fun onCommand(sender: Player) {
        val playerID = sender.uniqueId

        val campement = campementService.getCampementByOwner(playerID)
        if (campement == null) {
            sender.sendMessage("§cTu n'as pas de campement !")
            return
        }

        if (borderRenderer.isShowingBorders(sender)) {
            borderRenderer.clearBorders(sender)
            sender.sendMessage("§eLes bordures de ton campement ont été désactivées.")
        } else {
            borderRenderer.showBorders(sender, campement.listChunks.map { it.split(",").map(String::toInt) })
            sender.sendMessage("§aLes bordures de ton campement sont maintenant visibles !")
        }
    }
}

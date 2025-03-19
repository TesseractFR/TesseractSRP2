package onl.tesseract.srp.controller.command.campement

import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.CommandBody
import onl.tesseract.srp.domain.campement.AnnexationStick
import org.bukkit.entity.Player
import org.springframework.stereotype.Component

@Component
@Command(name = "stick", playerOnly = true)
class AnnexionStickCommand {

    @CommandBody
    fun onCommand(sender: Player) {
        AnnexationStick.giveToPlayer(sender)
        sender.sendMessage("§6Tu as reçu un §eBâton d'Annexion§6 !")
    }
}

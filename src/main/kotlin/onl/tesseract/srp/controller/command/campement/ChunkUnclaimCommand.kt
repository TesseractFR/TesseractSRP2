package onl.tesseract.srp.controller.command.campement

import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.CommandBody
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component

@Component
@Command(name = "unclaim", playerOnly = true)
class ChunkUnclaimCommand(private val campementService: CampementService) {

    @CommandBody
    fun onCommand(sender: Player) {
        val playerID = sender.uniqueId
        val chunk = "${sender.location.chunk.x},${sender.location.chunk.z}"
        val campement = campementService.getCampementByOwner(playerID)
        if (campement == null) {
            sender.sendMessage("§cTu n'as pas de campement !")
            return
        }
        val success = campementService.unclaimChunk(playerID, chunk)
        if (success) {
            sender.sendMessage("§aLe chunk ($chunk) a été retiré de ton campement !")
        } else {
            sender.sendMessage("§cCe chunk ne fait pas partie de ton campement.")
        }
    }
}

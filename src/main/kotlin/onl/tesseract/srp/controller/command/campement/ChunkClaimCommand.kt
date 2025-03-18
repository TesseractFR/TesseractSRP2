package onl.tesseract.srp.controller.command.campement

import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.CommandBody
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component

@Component
@Command(name = "claim", playerOnly = true)
class ChunkClaimCommand(private val campementService: CampementService) {

    @CommandBody
    fun onCommand(sender: Player) {
        val chunk = "${sender.location.chunk.x},${sender.location.chunk.z}"
        sender.sendMessage(campementService.handleClaimUnclaim(sender.uniqueId, chunk, true))
    }
}

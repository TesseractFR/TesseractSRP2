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
        val playerID = sender.uniqueId
        val chunk = "${sender.location.chunk.x},${sender.location.chunk.z}"

        val campement = campementService.getCampementByOwner(playerID)
        if (campement == null) {
            sender.sendMessage("§cTu n'as pas encore de campement ! Crée-en un d'abord avec /camp create.")
            return
        }

        val annexationResult = campementService.claimChunk(playerID, chunk)

        when (annexationResult) {
            CampementService.AnnexationResult.SUCCESS -> {
                sender.sendMessage("§aLe chunk ($chunk) a été annexé avec succès !")
            }
            CampementService.AnnexationResult.ALREADY_OWNED -> {
                sender.sendMessage("§eTu possèdes déjà ce chunk.")
            }
            CampementService.AnnexationResult.ALREADY_CLAIMED -> {
                sender.sendMessage("§cCe chunk est déjà revendiqué par un autre campement.")
            }
            CampementService.AnnexationResult.NOT_ADJACENT -> {
                sender.sendMessage("§cCe chunk n'est pas adjacent à ton campement.")
            }
        }
    }
}

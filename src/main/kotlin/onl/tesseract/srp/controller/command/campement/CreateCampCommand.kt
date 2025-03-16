package onl.tesseract.srp.controller.command.campement

import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.CommandBody
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component

@Component
@Command(name = "create", playerOnly = true)
class CreateCampCommand(private val campementService: CampementService) {

    @CommandBody
    fun onCommand(sender: Player) {
        val playerID = sender.uniqueId
        val world = sender.world
        val location = sender.location
        val centerChunk = sender.location.chunk

        val chunks = listOf(
            centerChunk,
            world.getChunkAt(centerChunk.x + 1, centerChunk.z),
            world.getChunkAt(centerChunk.x, centerChunk.z + 1),
            world.getChunkAt(centerChunk.x + 1, centerChunk.z + 1)
        )

        val chunkList = chunks.map { "${it.x},${it.z}" }

        val existingCampement = campementService.getCampementByOwner(playerID)
        if (existingCampement != null) {
            sender.sendMessage("§cTu possèdes déjà un campement !")
            return
        }

        // Création du campement
        campementService.createCampement(playerID, chunkList, location)
        sender.sendMessage("§aCampement créé avec succès ! Tu contrôles maintenant ${chunkList.size} chunks.")

    }

}

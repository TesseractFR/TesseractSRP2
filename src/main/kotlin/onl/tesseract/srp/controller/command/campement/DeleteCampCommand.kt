package onl.tesseract.srp.controller.command.campement

import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.CommandBody
import onl.tesseract.lib.chat.ChatEntryService
import onl.tesseract.lib.service.ServiceContainer
import onl.tesseract.srp.service.campement.CampementBorderRenderer
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component
import java.util.*
import net.kyori.adventure.text.Component as Component1

/**
 * Delete a camp
 */
@Component
@Command(name = "delete", playerOnly = true)
class DeleteCampCommand(
    private val campementService: CampementService,
    private val borderRenderer: CampementBorderRenderer,
    private val chatEntryService: ChatEntryService
) {
    private val pendingDeletions = mutableSetOf<UUID>()

    @CommandBody
    fun onCommand(sender: Player) {
        val playerID = sender.uniqueId

        val existingCampement = campementService.getCampementByOwner(playerID)
        if (existingCampement == null) {
            sender.sendMessage("§cTu ne possèdes pas de campement à supprimer !")
            return
        }
        sender.sendMessage("§e⚠ Es-tu sûr de vouloir supprimer ton campement ?")

        val confirmButton = Component1.text("[Oui]", NamedTextColor.GREEN)
            .clickEvent(chatEntryService.clickCommand(sender) {
                if (pendingDeletions.remove(playerID)) { // Empêche le double message
                    campementService.deleteCampement(existingCampement.id)
                    borderRenderer.clearBorders(sender)
                    sender.sendMessage("§aTon campement a été supprimé avec succès.")
                }
            })

        val cancelButton = Component1.text("[Non]", NamedTextColor.RED)
            .clickEvent(chatEntryService.clickCommand(sender) {
                if (pendingDeletions.remove(playerID)) { // Supprime la demande en attente
                    sender.sendMessage("§eSuppression annulée.")
                }
            })

        sender.sendMessage(confirmButton.append(Component1.text(" ")).append(cancelButton))

        pendingDeletions.add(playerID)
    }
}

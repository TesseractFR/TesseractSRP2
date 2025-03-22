package onl.tesseract.srp.controller.command.campement

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.CommandBody
import onl.tesseract.lib.chat.ChatEntryService
import onl.tesseract.lib.util.ChatFormats
import onl.tesseract.srp.service.campement.CampementBorderRenderer
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component as SpringComponent
import java.util.*


@SpringComponent
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
        val existingCampement = campementService.getCampementOrWarn(sender) ?: return

        sender.sendMessage(ChatFormats.CHAT.append(Component.text("⚠ Es-tu sûr de vouloir supprimer ton campement ?")))

        val confirmButton = Component.text("[Oui]", NamedTextColor.GREEN)
            .clickEvent(chatEntryService.clickCommand(sender) {
                if (pendingDeletions.remove(playerID)) {
                    campementService.deleteCampement(existingCampement.id)
                    borderRenderer.clearBorders(sender)
                    sender.sendMessage(ChatFormats.CHAT_SUCCESS.append(Component.text("Ton campement a été supprimé avec succès.")))
                }
            })

        val cancelButton = Component.text("[Non]", NamedTextColor.RED)
            .clickEvent(chatEntryService.clickCommand(sender) {
                if (pendingDeletions.remove(playerID)) {
                    sender.sendMessage(ChatFormats.CHAT.append(Component.text("Suppression annulée.")))
                }
            })

        sender.sendMessage(confirmButton.append(Component.text(" ")).append(cancelButton))

        pendingDeletions.add(playerID)
    }
}

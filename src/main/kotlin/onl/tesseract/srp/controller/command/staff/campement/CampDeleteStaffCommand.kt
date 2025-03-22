package onl.tesseract.srp.controller.command.staff.campement

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.commandBuilder.annotation.*
import onl.tesseract.lib.chat.ChatEntryService
import onl.tesseract.lib.util.ChatFormats
import onl.tesseract.srp.controller.command.argument.CampOwnerArg
import onl.tesseract.srp.service.campement.CampementBorderRenderer
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.springframework.stereotype.Component as SpringComponent
import java.util.*

@SpringComponent
@Command(
    name = "delete",
    args = [Argument("joueur", clazz = CampOwnerArg::class)]
)
class CampDeleteStaffCommand(
    private val campementService: CampementService,
    private val borderRenderer: CampementBorderRenderer,
    private val chatEntryService: ChatEntryService
) {
    private val pendingDeletions = mutableMapOf<UUID, UUID>()

    @CommandBody
    fun onCommand(sender: Player, @Env(key = "joueur") ownerName: String) {
        val owner = Bukkit.getOfflinePlayerIfCached(ownerName)
            ?: Bukkit.getOfflinePlayer(ownerName)
        val campement = campementService.getCampementByOwner(owner.uniqueId)
        if (campement == null) {
            sender.sendMessage(
                ChatFormats.CHAT_ERROR.append(Component.text("${owner.name} ne possède pas de campement."))
            )
            return
        }

        sender.sendMessage(
            ChatFormats.CHAT.append(
                Component.text("⚠ Es-tu sûr de vouloir supprimer le campement de ${owner.name} ?")
            )
        )

        val confirmButton = Component.text("[Oui]", NamedTextColor.GREEN)
            .clickEvent(chatEntryService.clickCommand(sender) {
                if (pendingDeletions.remove(sender.uniqueId) == owner.uniqueId) {
                    campementService.deleteCampement(campement.id)
                    owner.player?.let { borderRenderer.clearBorders(it) }
                    sender.sendMessage(
                        ChatFormats.CHAT_SUCCESS.append(
                            Component.text("Le campement de ${owner.name} a été supprimé avec succès.")
                        )
                    )
                    owner.player?.sendMessage(
                        ChatFormats.CHAT_ERROR.append(Component.text("Ton campement a été supprimé par un administrateur."))
                    )
                }
            })

        val cancelButton = Component.text("[Non]", NamedTextColor.RED)
            .clickEvent(chatEntryService.clickCommand(sender) {
                if (pendingDeletions.remove(sender.uniqueId) == owner.uniqueId) {
                    sender.sendMessage(ChatFormats.CHAT.append(Component.text("Suppression annulée.")))
                }
            })

        sender.sendMessage(confirmButton.append(Component.text(" ")).append(cancelButton))
        pendingDeletions[sender.uniqueId] = owner.uniqueId
    }
}

package onl.tesseract.srp.controller.command.campement

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.CommandBody
import onl.tesseract.lib.util.ChatFormats
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component as SpringComponent

/**
 * Teleport to the player's camp.
 */
@SpringComponent
@Command(name = "spawn", playerOnly = true)
class CampSpawnCommand(private val campementService: CampementService) {

    @CommandBody
    fun onCommand(sender: Player) {
        val campement = campementService.getCampementOrWarn(sender) ?: return
        val spawnLocation = campement.spawnLocation
        sender.teleport(spawnLocation)
        sender.sendMessage(
            ChatFormats.CHAT_SUCCESS.append(
                Component.text("Tu as été téléporté à ton campement !")
            )
        )
    }
}

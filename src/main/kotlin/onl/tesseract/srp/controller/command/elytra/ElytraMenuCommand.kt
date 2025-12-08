package onl.tesseract.srp.controller.command.elytra

import onl.tesseract.commandBuilder.CommandContext
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.CommandBody
import onl.tesseract.lib.profile.PlayerProfileService
import onl.tesseract.srp.SrpCommandInstanceProvider
import onl.tesseract.srp.controller.menu.elytra.ElytraMenu
import onl.tesseract.srp.service.equipment.elytra.ElytraService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component as SpringComponent

@Command(name = "elytras", description = "Ouvre le menu des ailes.")
@SpringComponent
class ElytraMenuCommand(
    private val elytraService: ElytraService,
    private val playerProfileService: PlayerProfileService,
    commandInstanceProvider: SrpCommandInstanceProvider
) : CommandContext(commandInstanceProvider) {
    @CommandBody
    fun onCommand(sender: Player) {
        elytraService.giveElytraIfMissing(sender.uniqueId)
        ElytraMenu(sender, elytraService, playerProfileService)
            .open(sender)
    }
}


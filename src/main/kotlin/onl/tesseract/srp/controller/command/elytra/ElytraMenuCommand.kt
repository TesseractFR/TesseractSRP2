package onl.tesseract.srp.controller.command.elytra

import onl.tesseract.commandBuilder.CommandContext
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.CommandBody
import onl.tesseract.lib.equipment.EquipmentService
import onl.tesseract.lib.profile.PlayerProfileService
import onl.tesseract.srp.SrpCommandInstanceProvider
import onl.tesseract.srp.controller.menu.elytra.ElytraMenu
import onl.tesseract.srp.service.elytra.ElytraService
import onl.tesseract.srp.service.player.SrpPlayerService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component as SpringComponent

@Command(name = "elytras", description = "Ouvre le menu des ailes.")
@SpringComponent
class ElytraMenuCommand(
    private val equipmentService: EquipmentService,
    private val elytraService: ElytraService,
    private val playerService: SrpPlayerService,
    private val playerProfileService: PlayerProfileService,
    commandInstanceProvider: SrpCommandInstanceProvider
) : CommandContext(commandInstanceProvider) {

    @CommandBody
    fun onCommand(sender: Player) {
        ElytraMenu(sender, equipmentService, elytraService, playerService, playerProfileService).open(sender)
    }
}

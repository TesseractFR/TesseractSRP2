package onl.tesseract.srp.controller.command.elytra

import onl.tesseract.commandBuilder.CommandContext
import onl.tesseract.commandBuilder.CommandInstanceProvider
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.CommandBody
import onl.tesseract.lib.profile.PlayerProfileService
import onl.tesseract.srp.controller.menu.elytra.ElytraUpgradeMenu
import onl.tesseract.srp.service.equipment.elytra.ElytraService
import onl.tesseract.srp.service.player.SrpPlayerService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component

@Component
@Command(name = "elytrasUpgrade", description = "Ouvre le menu des améliorations des ailes", playerOnly = true)
class ElytraUpgradeMenuCommand(
    private val playerProfileService: PlayerProfileService,
    private val elytraService: ElytraService,
    private val srpPlayerService: SrpPlayerService,
    provider: CommandInstanceProvider
) : CommandContext(provider) {
    @CommandBody
    fun execute(player: Player) {
        ElytraUpgradeMenu(player.uniqueId, playerProfileService, elytraService, srpPlayerService, null)
            .open(player)
    }
}

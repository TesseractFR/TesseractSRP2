package onl.tesseract.srp.controller.command.elytra

import onl.tesseract.commandBuilder.CommandContext
import onl.tesseract.commandBuilder.CommandInstanceProvider
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.CommandBody
import onl.tesseract.lib.profile.PlayerProfileService
import onl.tesseract.srp.controller.menu.elytra.ElytraUpgradeMenu
import onl.tesseract.srp.service.equipment.elytra.ElytraService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component

@Component
@Command(name = "elytrasUpgrade", description = "Ouvre le menu des améliorations des ailes", playerOnly = true)
class ElytraUpgradeCommand(
    private val playerProfileService: PlayerProfileService,
    private val elytraService: ElytraService,
    provider: CommandInstanceProvider
) : CommandContext(provider) {
    @CommandBody
    fun execute(player: Player) {
        elytraService.giveElytraIfMissing(player.uniqueId)
        ElytraUpgradeMenu(
            player.uniqueId,
            playerProfileService,
            elytraService,
            null
        ).open(player)
    }
}


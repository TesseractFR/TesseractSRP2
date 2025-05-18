package onl.tesseract.srp.controller.command.elytra

import onl.tesseract.commandBuilder.CommandContext
import onl.tesseract.commandBuilder.CommandInstanceProvider
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.CommandBody
import onl.tesseract.lib.profile.PlayerProfileService
import onl.tesseract.srp.controller.menu.elytra.ElytraUpgradeSelectionMenu
import onl.tesseract.srp.controller.menu.elytra.ElytraUpgradeMenu
import onl.tesseract.srp.service.player.SrpPlayerService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component

@Component
@Command(name = "elytrasUpgrade", description = "Ouvre le menu des améliorations des ailes", playerOnly = true)
class ElytraUpgradeCommand(
    private val playerService: SrpPlayerService,
    private val playerProfileService: PlayerProfileService,
    provider: CommandInstanceProvider
) : CommandContext(provider) {

    @CommandBody
    fun execute(player: Player) {
        lateinit var selectionMenu: ElytraUpgradeSelectionMenu

        selectionMenu = ElytraUpgradeSelectionMenu("Améliorations d'élytra") { viewer, upgrade ->
            ElytraUpgradeMenu(
                viewer.uniqueId,
                playerService,
                playerProfileService,
                upgrade,
                selectionMenu
            ).open(viewer)
        }

        selectionMenu.open(player)
    }

}

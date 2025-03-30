package onl.tesseract.srp.controller.command.rank

import onl.tesseract.commandBuilder.CommandContext
import onl.tesseract.commandBuilder.CommandInstanceProvider
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.CommandBody
import onl.tesseract.lib.profile.PlayerProfileService
import onl.tesseract.srp.controller.menu.player.PlayerRankProgressMenu
import onl.tesseract.srp.service.player.SrpPlayerService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component

@Command(name = "rank", playerOnly = true)
@Component
class RankCommand(
    provider: CommandInstanceProvider,
    private val playerService: SrpPlayerService,
    private val playerProfileService: PlayerProfileService
) : CommandContext(provider) {

    @CommandBody
    fun openMenu(sender: Player) {
        PlayerRankProgressMenu(sender.uniqueId, playerService, playerProfileService, null).open(sender)
    }
}
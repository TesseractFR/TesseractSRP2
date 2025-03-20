package onl.tesseract.srp.controller.command.staff

import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.commandBuilder.annotation.Argument
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.lib.command.argument.OfflinePlayerArg
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.controller.command.argument.PlayerRankArg
import onl.tesseract.srp.service.player.SrpPlayerService
import org.bukkit.command.CommandSender
import org.springframework.stereotype.Component

@Command(name = "rank")
@Component
class PlayerRankStaffCommand(private val playerService: SrpPlayerService) {

    @Command(name = "set")
    fun set(@Argument("player") playerArg: OfflinePlayerArg, @Argument("rank") rankArg: PlayerRankArg, sender: CommandSender) {
        val player = playerArg.get()
        playerService.setRank(player.uniqueId, rankArg.get())
        sender.sendMessage(NamedTextColor.GREEN + "${player.name} a maintenant le rang ${rankArg.get()}")
    }
}
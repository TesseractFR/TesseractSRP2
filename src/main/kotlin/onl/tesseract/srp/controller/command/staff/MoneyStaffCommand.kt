package onl.tesseract.srp.controller.command.staff

import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.commandBuilder.annotation.Argument
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.lib.command.argument.IntegerCommandArgument
import onl.tesseract.lib.command.argument.OfflineUUIDPlayerArg
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.service.player.SrpPlayerService
import org.bukkit.command.CommandSender
import org.springframework.stereotype.Component

@Command(name = "money")
@Component
class MoneyStaffCommand(private val playerService: SrpPlayerService) {

    @Command(name = "get")
    fun get(
        sender: CommandSender,
        @Argument("player") playerArg: OfflineUUIDPlayerArg
    ) {
        val player = playerService.getPlayer(playerArg.get().uniqueId)
        sender.sendMessage("${player.money} Lys")
    }

    @Command(name = "give", description = "Donner de l'argent à un joueur")
    fun giveMoney(
        @Argument("player") playerArg: OfflineUUIDPlayerArg,
        @Argument("amount") amountArg: IntegerCommandArgument,
        sender: CommandSender
    ) {
        val result = playerService.giveMoneyAsStaff(playerArg.get().uniqueId, amountArg.get())
        if (result) {
            sender.sendMessage(NamedTextColor.GREEN + "Argent ajouté")
        } else {
            sender.sendMessage(NamedTextColor.RED + "Le joueur n'a pas assez d'argent")
        }
    }

    @Command(name = "take", description = "Retirer de l'argent à un joueur")
    fun takeMoney(
        @Argument("player") playerArg: OfflineUUIDPlayerArg,
        @Argument("amount") amountArg: IntegerCommandArgument,
        sender: CommandSender
    ) {
        val result = playerService.giveMoneyAsStaff(playerArg.get().uniqueId, -amountArg.get())
        if (result) {
            sender.sendMessage(NamedTextColor.GREEN + "Argent retiré")
        } else {
            sender.sendMessage(NamedTextColor.RED + "Le joueur n'a pas assez d'argent")
        }
    }
}
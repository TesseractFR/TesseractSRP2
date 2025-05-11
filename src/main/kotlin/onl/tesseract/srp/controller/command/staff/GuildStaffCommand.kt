package onl.tesseract.srp.controller.command.staff

import onl.tesseract.commandBuilder.annotation.Argument
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.lib.command.argument.IntegerCommandArgument
import onl.tesseract.srp.controller.command.argument.GuildArg
import onl.tesseract.srp.service.guild.GuildService
import org.bukkit.command.CommandSender
import org.springframework.stereotype.Component

@Component
@Command(name = "guild")
class GuildStaffCommand {

    @Command(name = "money", description = "Gérer l'argent d'une guilde")
    @Component
    class MoneyCommand(private val guildService: GuildService) {

        @Command(name = "give")
        fun giveMoney(
            @Argument("guild") guildArg: GuildArg,
            @Argument("amount") amountArg: IntegerCommandArgument,
            sender: CommandSender
        ) {
            guildService.giveMoneyAsStaff(guildArg.get().id, amountArg.get())
            sender.sendMessage("Opération effectuée")
        }

        @Command(name = "take")
        fun takeMoney(
            @Argument("guild") guildArg: GuildArg,
            @Argument("amount") amountArg: IntegerCommandArgument,
            sender: CommandSender
        ) {
            guildService.giveMoneyAsStaff(guildArg.get().id, -amountArg.get())
            sender.sendMessage("Opération effectuée")
        }

        @Command(name = "get")
        fun getMoney(@Argument("guild") guildArg: GuildArg, sender: CommandSender) {
            val money = guildArg.get().money
            val name = guildArg.get().name
            sender.sendMessage("$name : $money Lys")
        }
    }

}

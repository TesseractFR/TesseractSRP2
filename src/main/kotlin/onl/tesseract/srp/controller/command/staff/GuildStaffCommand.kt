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

    @Command(name = "level", description = "Gérer le niveau d'une guilde")
    @Component
    class LevelCommand(private val guildService: GuildService) {
        @Command(name = "set")
        fun setLevel(
            @Argument("guild") guildArg: GuildArg,
            @Argument("level") levelArg: IntegerCommandArgument,
            sender: CommandSender
        ) {
            guildService.setLevel(guildArg.get().id, levelArg.get())
            sender.sendMessage("Opération effectuée - niveau de ${guildArg.get().name} défini à ${levelArg.get()}")
        }

        @Command(name = "get")
        fun getLevel(@Argument("guild") guildArg: GuildArg, sender: CommandSender) {
            val level = guildArg.get().level
            val name = guildArg.get().name
            sender.sendMessage("$name : niveau $level")
        }

        @Command(name = "addXp")
        fun addXp(
            @Argument("guild") guildArg: GuildArg,
            @Argument("amount") xpArg: IntegerCommandArgument,
            sender: CommandSender
        ) {
            guildService.addGuildXp(guildArg.get().id, xpArg.get())
            sender.sendMessage("Opération effectuée - ${xpArg.get()} XP ajoutés à ${guildArg.get().name}")
        }
    }

}

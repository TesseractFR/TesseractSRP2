package onl.tesseract.srp.controller.command.guild

import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.commandBuilder.CommandContext
import onl.tesseract.commandBuilder.CommandInstanceProvider
import onl.tesseract.commandBuilder.annotation.Argument
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.lib.chat.ChatEntryService
import onl.tesseract.lib.command.argument.StringArg
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.controller.menu.guild.GuildMenu
import onl.tesseract.srp.repository.hibernate.guild.GuildRepository
import onl.tesseract.srp.service.guild.GuildCreationResult
import onl.tesseract.srp.service.guild.GuildService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component

@Command(name = "guild")
@Component
class GuildCommand(
    provider: CommandInstanceProvider,
    private val guildService: GuildService,
    private val guildRepository: GuildRepository,
    private val chatEntryService: ChatEntryService
) : CommandContext(provider) {

    @Command(name = "create", playerOnly = true)
    fun createGuild(sender: Player, @Argument("nom") nameArg: StringArg) {
        val (guild, errorReason) = guildService.createGuild(sender.uniqueId, sender.location, nameArg.get())

        errorReason.map { reason ->
            return@map when (reason) {
                GuildCreationResult.Reason.NotEnoughMoney -> NamedTextColor.RED + "Tu n'a pas assez d'argent"
                GuildCreationResult.Reason.InvalidWorld ->
                    NamedTextColor.RED + "Tu ne peux pas créer de guilde dans ce monde"

                GuildCreationResult.Reason.NearSpawn -> NamedTextColor.RED + "Tu es trop proche du spawn"
                GuildCreationResult.Reason.NearGuild -> NamedTextColor.RED + "Tu es trop proche d'une autre guilde"
                GuildCreationResult.Reason.NameTaken -> NamedTextColor.RED + "Ce nom est déjà pris"
                GuildCreationResult.Reason.PlayerHasGuild -> NamedTextColor.RED + "Tu as déjà une guilde"
                GuildCreationResult.Reason.Rank ->
                    NamedTextColor.RED + "Tu n'as pas le grade nécessaire pour créer une guilde (Baron)"
            }
        }.forEach { message -> sender.sendMessage(message) }

        if (guild != null) {
            sender.sendMessage("Guilde créée")
        }
    }

    @Command(name = "menu", playerOnly = true)
    fun openMenu(sender: Player) {
        GuildMenu(sender.uniqueId, guildService, guildRepository, chatEntryService)
            .open(sender)
    }
}

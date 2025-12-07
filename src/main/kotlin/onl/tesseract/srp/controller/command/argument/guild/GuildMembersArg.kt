package onl.tesseract.srp.controller.command.argument.guild

import onl.tesseract.commandBuilder.CommandArgument
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps
import onl.tesseract.lib.service.ServiceContainer
import onl.tesseract.srp.service.territory.guild.GuildService
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class GuildMembersArg(name: String) : CommandArgument<String>(name) {
    override fun define(builder: CommandArgumentBuilderSteps.Parser<String>) {
        builder
            .parser { input, _ -> input }
            .tabCompleter { _, env ->
                val service = ServiceContainer[GuildService::class.java]
                val player = env.sender as? Player
                        ?: return@tabCompleter emptyList()
                val guild = service.getGuildByMember(player.uniqueId)
                    ?: return@tabCompleter emptyList()
                val memberNames = guild.members
                    .asSequence()
                    .mapNotNull { Bukkit.getOfflinePlayer(it.playerID).name }
                    .distinct()
                    .sorted()
                    .toList()
                listOf("<Membre_de_${guild.name}>") + memberNames
            }
    }
}

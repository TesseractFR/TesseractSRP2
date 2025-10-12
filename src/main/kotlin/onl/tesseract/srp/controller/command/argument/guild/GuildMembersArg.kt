package onl.tesseract.srp.controller.command.argument.guild

import onl.tesseract.commandBuilder.CommandArgument
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps
import onl.tesseract.lib.service.ServiceContainer
import onl.tesseract.srp.domain.guild.Guild
import onl.tesseract.srp.service.guild.GuildService
import org.bukkit.Bukkit

class GuildMembersArg(name: String) : CommandArgument<String>(name) {
    override fun define(builder: CommandArgumentBuilderSteps.Parser<String>) {
        builder
            .parser { input, _ -> input }
            .tabCompleter { _, env ->
                val service = ServiceContainer[GuildService::class.java]
                val guildFromArg: Guild? = env.get("guild", Guild::class.java)
                val guild: Guild = guildFromArg ?: run {
                    val guildName = env.get("guild", String::class.java) ?: return@tabCompleter listOf()
                    service.getAllGuilds().firstOrNull { it.name.equals(guildName, true) }
                        ?: return@tabCompleter listOf()
                }
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

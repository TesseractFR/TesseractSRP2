package onl.tesseract.srp.controller.command.argument

import onl.tesseract.commandBuilder.CommandArgument
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps
import onl.tesseract.lib.service.ServiceContainer
import onl.tesseract.srp.service.guild.GuildService
import org.bukkit.Bukkit

class GuildMembersArg(name: String) : CommandArgument<String>(name) {
    override fun define(builder: CommandArgumentBuilderSteps.Parser<String>) {
        builder.parser { input, _ -> input }
            .tabCompleter { _, env ->
                val repo = ServiceContainer[GuildService::class.java]
                val leaderName = env.get("leader", String::class.java) ?: env.senderAsPlayer.name
                val leaderId = Bukkit.getOfflinePlayer(leaderName).uniqueId
                val guild = repo.getGuildByLeader(leaderId) ?: return@tabCompleter listOf()

                val names = guild.members
                    .mapNotNull { Bukkit.getOfflinePlayer(it.playerID).name }
                    .filter { it != leaderName }
                    .distinct()

                listOf("<Membre_de_ta_guilde>") + names
            }
    }
}

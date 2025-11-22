// onl.tesseract.srp.controller.command.argument.GuildRoleArg.kt
package onl.tesseract.srp.controller.command.argument.guild

import onl.tesseract.commandBuilder.CommandArgument
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps
import onl.tesseract.commandBuilder.CommandArgumentException
import onl.tesseract.srp.domain.territory.guild.enum.GuildRole

class GuildMembersRoleArg(name: String) : CommandArgument<GuildRole>(name) {
    override fun define(builder: CommandArgumentBuilderSteps.Parser<GuildRole>) {
        val all = GuildRole.entries.map { it.name }
        builder
            .parser { input, _ ->
                GuildRole.entries.firstOrNull { it.name.equals(input, true) }
                    ?: throw CommandArgumentException("Rôle inconnu: $input. Valeurs: ${all.joinToString()}")
            }
            .tabCompleter { prefix, _ ->
                val p = (prefix ?: "").lowercase()
                listOf("<Role>") + all.filter { it.lowercase().startsWith(p) }
            }
    }
}

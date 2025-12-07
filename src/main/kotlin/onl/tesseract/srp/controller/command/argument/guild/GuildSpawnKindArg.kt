package onl.tesseract.srp.controller.command.argument.guild

import onl.tesseract.commandBuilder.CommandArgument
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps
import onl.tesseract.srp.domain.territory.guild.enum.GuildSpawnKind

class GuildSpawnKindArg(name: String) : CommandArgument<GuildSpawnKind>(name) {
    override fun define(b: CommandArgumentBuilderSteps.Parser<GuildSpawnKind>) {
        b.parser { input, _ ->
            when (input.lowercase()) {
                "private", "privé", "prive" -> GuildSpawnKind.PRIVATE
                "visitor", "visiteur"       -> GuildSpawnKind.VISITOR
                else -> throw IllegalArgumentException("Type inconnu: $input")
            }
        }.tabCompleter { _, _ -> listOf("private", "visitor") }
    }
}

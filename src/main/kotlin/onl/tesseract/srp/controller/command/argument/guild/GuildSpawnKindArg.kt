package onl.tesseract.srp.controller.command.argument.guild

import onl.tesseract.commandBuilder.CommandArgument
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps
import onl.tesseract.srp.service.guild.GuildService

class GuildSpawnKindArg(name: String) : CommandArgument<GuildService.GuildSpawnKind>(name) {
    override fun define(b: CommandArgumentBuilderSteps.Parser<GuildService.GuildSpawnKind>) {
        b.parser { input, _ ->
            when (input.lowercase()) {
                "private", "privé", "prive" -> GuildService.GuildSpawnKind.PRIVATE
                "visitor", "visiteur"       -> GuildService.GuildSpawnKind.VISITOR
                else -> throw IllegalArgumentException("Type inconnu: $input")
            }
        }.tabCompleter { _, _ -> listOf("private", "visitor") }
    }
}

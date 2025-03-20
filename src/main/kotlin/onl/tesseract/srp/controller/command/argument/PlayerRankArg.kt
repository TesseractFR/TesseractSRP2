package onl.tesseract.srp.controller.command.argument

import onl.tesseract.commandBuilder.CommandArgument
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps
import onl.tesseract.srp.domain.player.PlayerRank

class PlayerRankArg(name: String) : CommandArgument<PlayerRank>(name) {

    override fun define(builder: CommandArgumentBuilderSteps.Parser<PlayerRank>) {
        builder.parser { input, _ -> PlayerRank.valueOf(input) }
            .tabCompleter { _, _ -> PlayerRank.entries.map { it.name } }
            .errorHandler(IllegalArgumentException::class.java, "Rang invalide")
    }
}
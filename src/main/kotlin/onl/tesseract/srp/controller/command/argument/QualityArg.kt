package onl.tesseract.srp.controller.command.argument

import onl.tesseract.commandBuilder.CommandArgument
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps
import onl.tesseract.srp.customitem.domain.model.Quality

class QualityArg (name: String) : CommandArgument<Quality>(name) {
    override fun define(builder: CommandArgumentBuilderSteps.Parser<Quality>) {
        builder.parser { input, _ -> Quality.valueOf(input) }
                .tabCompleter { _, _ ->
                    Quality.entries.map { it.name }
                }
    }
}
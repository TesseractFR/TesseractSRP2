package onl.tesseract.srp.controller.command.staff

import onl.tesseract.commandBuilder.CommandArgument
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps
import onl.tesseract.srp.domain.item.CustomMaterial

class CustomMaterialArg(name: String) : CommandArgument<CustomMaterial>(name) {

    override fun define(builder: CommandArgumentBuilderSteps.Parser<CustomMaterial>) {
        builder.parser { input, _ -> CustomMaterial.valueOf(input) }
            .tabCompleter { _, _ -> CustomMaterial.entries.map { it.name }}
            .errorHandler(IllegalArgumentException::class.java, "Matériau invalide")
    }
}
package onl.tesseract.srp.controller.command.argument

import onl.tesseract.commandBuilder.CommandArgument
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps
import onl.tesseract.lib.service.ServiceContainer
import onl.tesseract.srp.customitem.adapter.userside.ItemGateway
import onl.tesseract.srp.customitem.domain.model.CustomMaterial
import onl.tesseract.srp.customitem.domain.model.MaterialName

class CustomMaterialArg(name: String) : CommandArgument<CustomMaterial>(name) {

    override fun define(builder: CommandArgumentBuilderSteps.Parser<CustomMaterial>) {
        builder.parser { input, _ -> ServiceContainer[ItemGateway::class.java].customMaterials[MaterialName(input)] }
            .tabCompleter { _, _ -> ServiceContainer[ItemGateway::class.java].customMaterials.keys.map { it.value() }}
            .errorHandler(IllegalArgumentException::class.java, "Matériau invalide")
    }
}
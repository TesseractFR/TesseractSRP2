package onl.tesseract.srp.controller.command.argument

import onl.tesseract.commandBuilder.CommandArgument
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps
import onl.tesseract.lib.event.equipment.invocable.EnumElytraUpgrade

class ElytraUpgradeArg(name: String) : CommandArgument<EnumElytraUpgrade>(name) {
    override fun define(builder: CommandArgumentBuilderSteps.Parser<EnumElytraUpgrade>) {
        builder.parser { input, _ -> EnumElytraUpgrade.valueOf(input.uppercase()) }
            .tabCompleter { _, _ -> EnumElytraUpgrade.entries.map { it.name.lowercase() } }
            .errorHandler(IllegalArgumentException::class.java, "Amélioration Elytra invalide")
    }
}

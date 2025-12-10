package onl.tesseract.srp.controller.command.argument

import onl.tesseract.commandBuilder.CommandArgument
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps
import onl.tesseract.lib.event.equipment.invocable.ElytraUpgrade

class ElytraUpgradeArg(name: String) : CommandArgument<ElytraUpgrade>(name) {
    override fun define(builder: CommandArgumentBuilderSteps.Parser<ElytraUpgrade>) {
        builder.parser { input, _ -> ElytraUpgrade.valueOf(input.uppercase()) }
            .tabCompleter { _, _ -> ElytraUpgrade.entries.map { it.name.lowercase() } }
            .errorHandler(IllegalArgumentException::class.java, "Amélioration Elytra invalide")
    }
}

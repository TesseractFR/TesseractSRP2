package onl.tesseract.srp.controller.command.argument

import onl.tesseract.commandBuilder.CommandArgument
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps
import onl.tesseract.srp.service.equipment.elytra.MAX_UPGRADE_LEVEL
import onl.tesseract.srp.service.equipment.elytra.MIN_UPGRADE_LEVEL

class ElytraUpgradeLevelArg(name: String) : CommandArgument<Int>(name) {
    override fun define(builder: CommandArgumentBuilderSteps.Parser<Int>) {
        builder
            .parser { input, _ ->
                val value = input.toInt()
                require(value in MIN_UPGRADE_LEVEL..MAX_UPGRADE_LEVEL) { "level out of range" }
                value
            }
            .tabCompleter { _, _ ->
                (MIN_UPGRADE_LEVEL..MAX_UPGRADE_LEVEL).map { it.toString() }
            }
            .errorHandler(
                IllegalArgumentException::class.java,
                "Le niveau doit être compris entre $MIN_UPGRADE_LEVEL et $MAX_UPGRADE_LEVEL."
            )
    }
}

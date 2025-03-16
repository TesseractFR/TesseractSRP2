package onl.tesseract.srp.controller.command.argument

import onl.tesseract.commandBuilder.CommandArgument
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps
import onl.tesseract.lib.service.ServiceContainer
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.Bukkit

class CampOwnerArg(name: String) : CommandArgument<String>(name) {
    override fun define(builder: CommandArgumentBuilderSteps.Parser<String>) {
        builder.parser { input, _ -> input }
            .tabCompleter { _, _ ->
                val service = ServiceContainer[CampementService::class.java]
                listOf("<Propriétaire>") + service.getAllCampements()
                    .mapNotNull { Bukkit.getOfflinePlayer(it.ownerID).name }
            }
    }
}

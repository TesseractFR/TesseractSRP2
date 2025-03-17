package onl.tesseract.srp.controller.command.argument

import onl.tesseract.commandBuilder.CommandArgument
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps
import onl.tesseract.srp.service.campement.CampementService
import onl.tesseract.lib.service.ServiceContainer
import org.bukkit.Bukkit

class TrustedPlayerArg(name: String) : CommandArgument<String>(name) {

    override fun define(builder: CommandArgumentBuilderSteps.Parser<String>) {
        builder.parser { input, _ -> input }

            .tabCompleter { _, env ->
                val campementService = ServiceContainer[CampementService::class.java]
                val campement = campementService.getCampementByOwner(env.senderAsPlayer.uniqueId) ?: return@tabCompleter emptyList()

                campement.trustedPlayers.mapNotNull { Bukkit.getOfflinePlayer(it).name }
            }

            .errorHandler(NullPointerException::class.java, "Joueur non trouvé dans ta liste de confiance")
    }
}

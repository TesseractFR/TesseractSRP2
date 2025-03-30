package onl.tesseract.srp.controller.command.argument

import onl.tesseract.commandBuilder.CommandArgument
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps
import onl.tesseract.lib.service.ServiceContainer
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.Bukkit

class TrustedPlayerArg(name: String) : CommandArgument<String>(name) {
    override fun define(builder: CommandArgumentBuilderSteps.Parser<String>) {
        builder.parser { input, _ -> input }
            .tabCompleter { _, env ->
            val service = ServiceContainer[CampementService::class.java]
            val campement = service.getCampementByOwner(env.senderAsPlayer.uniqueId) ?: return@tabCompleter listOf()
            listOf("<Joueur_de_Confiance>") + campement.trustedPlayers.mapNotNull {
                Bukkit.getOfflinePlayer(it).name
            }
        }
    }
}

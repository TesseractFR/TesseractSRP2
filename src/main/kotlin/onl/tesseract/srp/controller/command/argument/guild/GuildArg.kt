package onl.tesseract.srp.controller.command.argument.guild

import onl.tesseract.commandBuilder.CommandArgument
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps
import onl.tesseract.commandBuilder.CommandArgumentException
import onl.tesseract.srp.PLUGIN_INSTANCE
import onl.tesseract.srp.domain.territory.guild.Guild
import onl.tesseract.srp.service.territory.guild.GuildService

class GuildArg(name: String) : CommandArgument<Guild>(name) {

    override fun define(builder: CommandArgumentBuilderSteps.Parser<Guild>) {
        builder.parser { input, _ ->
            guildService().getByName(input) ?: throw CommandArgumentException("Guilde $input introuvable")
        }
            .tabCompleter { _, _ -> guildService().getAllGuilds().map { it.name } }
    }

    private fun guildService(): GuildService =
        PLUGIN_INSTANCE.springContext.getBean(GuildService::class.java)
}

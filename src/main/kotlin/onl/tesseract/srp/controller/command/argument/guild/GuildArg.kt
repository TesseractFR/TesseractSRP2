package onl.tesseract.srp.controller.command.argument.guild

import onl.tesseract.commandBuilder.CommandArgument
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps
import onl.tesseract.commandBuilder.CommandArgumentException
import onl.tesseract.srp.PLUGIN_INSTANCE
import onl.tesseract.srp.domain.guild.Guild
import onl.tesseract.srp.repository.hibernate.guild.GuildRepository

class GuildArg(name: String) : CommandArgument<Guild>(name) {

    override fun define(builder: CommandArgumentBuilderSteps.Parser<Guild>) {
        builder.parser { input, _ ->
            guildRepository().findGuildByName(input) ?: throw CommandArgumentException("Guilde $input introuvable")
        }
            .tabCompleter { _, _ -> guildRepository().findAll().map { it.name } }
    }

    private fun guildRepository(): GuildRepository =
        PLUGIN_INSTANCE.springContext.getBean(GuildRepository::class.java)
}

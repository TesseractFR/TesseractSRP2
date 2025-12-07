package onl.tesseract.srp.controller.command.argument.guild

import onl.tesseract.commandBuilder.CommandArgument
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps
import onl.tesseract.srp.domain.territory.guild.enum.GuildRank

class GuildRankArg(name: String) : CommandArgument<GuildRank>(name) {
    override fun define(builder: CommandArgumentBuilderSteps.Parser<GuildRank>) {
        val allTitles = GuildRank.entries.map { it.title }
        val byTitleCI = GuildRank.entries.associateBy { it.title.lowercase() }

        builder
            .parser { input, _ ->
                val key = input.trim().lowercase()
                byTitleCI[key] ?: throw IllegalArgumentException(
                    "Rang inconnu: \"$input\". Valeurs: ${allTitles.joinToString(", ")}"
                )
            }
            .tabCompleter { prefix, _ ->
                val p = (prefix ?: "").lowercase()
                listOf("<Rang>") + allTitles.filter { it.lowercase().startsWith(p) }
            }
    }
}

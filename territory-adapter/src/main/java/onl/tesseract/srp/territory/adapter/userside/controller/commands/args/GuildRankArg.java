package onl.tesseract.srp.territory.adapter.userside.controller.commands.args;

import onl.tesseract.commandBuilder.CommandArgument;
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.srp.territory.adapter.userside.controller.display.GuildRankDisplay;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildRank;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Command(name = "guildRank")
public class GuildRankArg extends CommandArgument<GuildRank> {
    public GuildRankArg(String name) {
        super(name);
    }

    @Override
    public void define(CommandArgumentBuilderSteps.Parser<GuildRank> builder) {
        List<String> allTitles = Arrays.stream(GuildRankDisplay.values())
                .map(GuildRankDisplay::getTitle)
                .toList();
        Map<String, GuildRank> byTitleCI = Arrays.stream(GuildRank.values())
                .collect(Collectors.toMap(
                        r -> GuildRankDisplay.of(r).getTitle().toLowerCase(),
                        r -> r));
        builder
                .parser((input, _) -> {
                    String key = input.trim().toLowerCase();
                    GuildRank guildRank = byTitleCI.get(key);
                    if (guildRank == null) {
                        throw new IllegalArgumentException(
                                "Rang inconnu: \"" + input + "\". Valeurs: " + String.join(", ", allTitles)
                        );
                    }
                    return guildRank;
                })
                .tabCompleter((prefix, _) -> {
                    String p = (prefix != null ? prefix : "").toLowerCase();
                    return Stream.concat(
                            Stream.of("<Rang>"),
                            allTitles.stream().filter(it -> it.toLowerCase().startsWith(p))
                    ).toList();
                });
    }
}
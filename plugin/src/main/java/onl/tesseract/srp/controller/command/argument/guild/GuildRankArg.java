package onl.tesseract.srp.controller.command.argument.guild;

import onl.tesseract.commandBuilder.CommandArgument;
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildRank;
import org.springframework.stereotype.Component;

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
        List<String> allTitles = Arrays.stream(GuildRank.values()).map(GuildRank::getTitle).toList();
        Map<String, GuildRank> byTitleCI = Arrays.stream(GuildRank.values()).collect(Collectors.toMap(g -> g.getTitle().toLowerCase(), g -> g));

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
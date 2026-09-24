package onl.tesseract.srp.controller.command.argument.guild;

import onl.tesseract.commandBuilder.CommandArgument;
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps;
import onl.tesseract.commandBuilder.CommandArgumentException;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildRole;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class GuildMembersRoleArg extends CommandArgument<GuildRole> {

    public GuildMembersRoleArg(String name) {
        super(name);
    }

    @Override
    public void define(CommandArgumentBuilderSteps.Parser<GuildRole> builder) {
        List<String> guildRoleNames = Arrays.stream(GuildRole.values()).map(GuildRole::name).toList();
        builder.parser((input,_)->{
            return Arrays.stream(GuildRole.values())
                    .filter(role -> role.name().equalsIgnoreCase(input))
                    .findFirst()
                    .orElseThrow(() -> new CommandArgumentException("Rôle inconnu: " + input + ". Valeurs: " + guildRoleNames));

        }).tabCompleter((prefix, _) -> {
            String p = (prefix != null ? prefix : "").toLowerCase();
            return Stream.concat(Stream.of("<Role>"), guildRoleNames.stream().filter(name -> name.toLowerCase().startsWith(p)))
                    .toList();
        });
    }
}

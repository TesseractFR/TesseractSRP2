package onl.tesseract.srp.controller.command.argument.guild;

import onl.tesseract.commandBuilder.CommandArgument;
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildSpawnKind;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

public class GuildSpawnKindArg extends CommandArgument<GuildSpawnKind> {

    public GuildSpawnKindArg(@NotNull String name) {
        super(name);
    }

    @Override
    public void define(CommandArgumentBuilderSteps.Parser<GuildSpawnKind> b) {
        b.parser((input, _) -> {
            switch (input.toLowerCase()) {
                case "private", "privé", "prive" -> {
                    return GuildSpawnKind.PRIVATE;
                }
                case "visitor", "visiteur" -> {
                    return GuildSpawnKind.VISITOR;
                }
                default -> throw new IllegalArgumentException("Unknown type: " + input);
            }
        }).tabCompleter((_, _) -> List.of("private", "visitor"));
    }
}
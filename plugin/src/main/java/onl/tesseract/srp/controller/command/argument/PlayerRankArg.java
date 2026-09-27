package onl.tesseract.srp.controller.command.argument;

import onl.tesseract.commandBuilder.CommandArgument;
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps;
import onl.tesseract.srp.common.domain.model.enums.PlayerRank;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PlayerRankArg extends CommandArgument<PlayerRank> {
    public PlayerRankArg(String name) {
        super(name);
    }

    @Override
    public void define(CommandArgumentBuilderSteps.Parser<PlayerRank> builder) {
        builder.parser((input, ignored) -> PlayerRank.valueOf(input))
                .tabCompleter((ignored, context) -> Arrays.stream(PlayerRank.values())
                        .map(Enum::name)
                        .collect(Collectors.toList()))
                .errorHandler(IllegalArgumentException.class, "Rang invalide");
    }
}
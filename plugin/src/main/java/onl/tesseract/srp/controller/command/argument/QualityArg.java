package onl.tesseract.srp.controller.command.argument;

import onl.tesseract.commandBuilder.CommandArgument;
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps;
import onl.tesseract.srp.common.domain.model.enums.Quality;

import java.util.Arrays;
import java.util.stream.Collectors;

public class QualityArg extends CommandArgument<Quality> {
    public QualityArg(String name) {
        super(name);
    }

    @Override
    public void define(CommandArgumentBuilderSteps.Parser<Quality> builder) {
        builder.parser((input, ignored) -> Quality.valueOf(input))
                .tabCompleter((ignored, context) -> Arrays.stream(Quality.values())
                        .map(Enum::name)
                        .collect(Collectors.toList()));
    }
}
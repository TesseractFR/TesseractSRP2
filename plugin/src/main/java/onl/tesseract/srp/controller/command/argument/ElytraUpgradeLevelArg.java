package onl.tesseract.srp.controller.command.argument;

import onl.tesseract.commandBuilder.CommandArgument;
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps;
import onl.tesseract.srp.service.equipment.elytra.Constants;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ElytraUpgradeLevelArg extends CommandArgument<Integer> {
    public ElytraUpgradeLevelArg(String name) {
        super(name);
    }

    @Override
    public void define(CommandArgumentBuilderSteps.Parser<Integer> builder) {
        builder.parser((input, _) -> {
                    int value = Integer.parseInt(input);
                    if (value < Constants.MIN_UPGRADE_LEVEL || value > Constants.MAX_UPGRADE_LEVEL) {
                        throw new IllegalArgumentException("level out of range");
                    }
                    return value;
                })
                .tabCompleter((_, _) -> IntStream.rangeClosed(Constants.MIN_UPGRADE_LEVEL, Constants.MAX_UPGRADE_LEVEL)
                        .mapToObj(String::valueOf)
                        .collect(Collectors.toList()))
                .errorHandler(IllegalArgumentException.class,
                        "Le niveau doit être compris entre " + Constants.MIN_UPGRADE_LEVEL + " et " + Constants.MAX_UPGRADE_LEVEL + ".");
    }
}

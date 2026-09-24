package onl.tesseract.srp.controller.command.argument;

import onl.tesseract.commandBuilder.CommandArgument;
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps;
import onl.tesseract.lib.event.equipment.invocable.ElytraUpgrade;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public class ElytraUpgradeArg extends CommandArgument<ElytraUpgrade> {
    public ElytraUpgradeArg(String name) {
        super(name);
    }

    @Override
    public void define(CommandArgumentBuilderSteps.Parser<ElytraUpgrade> builder) {
        builder.parser((input, _) -> ElytraUpgrade.valueOf(input.toUpperCase(Locale.ROOT)))
                .tabCompleter((_, _) -> Arrays.stream(ElytraUpgrade.values())
                        .map(it -> it.name().toLowerCase(Locale.ROOT))
                        .collect(Collectors.toList()))
                .errorHandler(IllegalArgumentException.class, "Amélioration Elytra invalide");
    }
}

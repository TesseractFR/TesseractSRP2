package onl.tesseract.srp.controller.command.argument;

import onl.tesseract.commandBuilder.CommandArgument;
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps;
import onl.tesseract.lib.service.ServiceContainer;
import onl.tesseract.srp.territory.domain.port.userside.campement.CampementService;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class CampOwnerArg extends CommandArgument<String> {
    public CampOwnerArg(@NotNull String name) {
        super(name);
    }

    @Override
    public void define(CommandArgumentBuilderSteps.Parser<String> builder) {
        builder.parser((input, _) -> input)
                .tabCompleter((_, _) -> {
                    CampementService service = ServiceContainer.get(CampementService.class);
                    List<String> campOwners = new ArrayList<>();
                    campOwners.add("<Propriétaire>");
                    campOwners.addAll(service.getAllCampements()
                            .stream()
                            .filter(Objects::nonNull)
                            .map(it -> Bukkit.getOfflinePlayer(it.getOwnerID()).getName()).toList());
                    return campOwners;
                });
    }
}



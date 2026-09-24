package onl.tesseract.srp.controller.command.argument;

import onl.tesseract.commandBuilder.CommandArgument;
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps;
import onl.tesseract.lib.service.ServiceContainer;
import onl.tesseract.srp.territory.domain.model.campement.Campement;
import onl.tesseract.srp.territory.domain.port.userside.campement.CampementService;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;


public class TrustedPlayerArg extends CommandArgument<String> {
    public TrustedPlayerArg(String name) {
        super(name);
    }

    @Override
    public void define(CommandArgumentBuilderSteps.Parser<String> builder){
        builder.parser((input, _)-> input)
                .tabCompleter((_, env)->{
                    CampementService service = ServiceContainer.get(CampementService.class);
                    String owner = env.get("owner", String.class);
                    String ownerName = owner != null ? owner : env.getSenderAsPlayer().getName();
                    Campement campement = service.getCampementByOwner(Bukkit.getOfflinePlayer(ownerName).getUniqueId());
                    if (campement == null) return new ArrayList<>();
                    List<String> trustedPlayers = new ArrayList<>();
                    trustedPlayers.add("<Joueur_de_Confiance>");
                    trustedPlayers.addAll(campement.getTrusted().stream().map(it -> Bukkit.getOfflinePlayer(it).getName()).toList());
                    return trustedPlayers;
                });
    }
}
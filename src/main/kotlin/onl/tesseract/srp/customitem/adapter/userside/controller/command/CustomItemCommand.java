package onl.tesseract.srp.customitem.adapter.userside.controller.command;

import onl.tesseract.commandBuilder.CommandContext;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.srp.customitem.domain.port.userside.CustomItemService;
import org.springframework.stereotype.Component;

@Component
@Command(name = "customitem", description = "Commande pour les customitem")
public class CustomItemCommand extends CommandContext {

    private final CustomItemService customItemService;


    public CustomItemCommand(CustomItemService customItemService) {
        this.customItemService = customItemService;
    }

    @Command(name = "reload", description = "Recharger les custom items.")
    public void reloadCustomItems() {
        customItemService.loadAll();
    }

}

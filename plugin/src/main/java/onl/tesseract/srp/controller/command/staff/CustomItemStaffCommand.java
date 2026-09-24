package onl.tesseract.srp.controller.command.staff;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.commandBuilder.annotation.Argument;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.lib.command.argument.IntegerCommandArgument;
import onl.tesseract.lib.command.argument.PlayerArg;
import onl.tesseract.srp.controller.command.argument.CustomItemArg;
import onl.tesseract.srp.controller.command.argument.QualityArg;
import onl.tesseract.srp.customitem.domain.port.userside.CustomItemService;
import org.bukkit.command.CommandSender;

@Command(name = "customitem", playerOnly = true)
@org.springframework.stereotype.Component
public class CustomItemStaffCommand {
    private final CustomItemService customItemService;

    public CustomItemStaffCommand(CustomItemService customItemService) {
        this.customItemService = customItemService;
    }

    @Command(name = "give")
    public void give(@Argument("player") PlayerArg playerArg,
                     @Argument("item") CustomItemArg itemArg,
                     @Argument("quality") QualityArg qualityArg,
                     @Argument("quantity") IntegerCommandArgument quantityArg,
                     CommandSender sender) {
        var player = playerArg.get();
        customItemService.addItem(player.getUniqueId(), itemArg.get().name(), qualityArg.get(), quantityArg.get());
        sender.sendMessage(Component.text( "Item donné",NamedTextColor.GREEN));
    }
}


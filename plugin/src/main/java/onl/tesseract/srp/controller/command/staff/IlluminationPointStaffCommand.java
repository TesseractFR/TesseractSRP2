package onl.tesseract.srp.controller.command.staff;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.commandBuilder.annotation.Argument;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.lib.command.argument.IntegerCommandArgument;
import onl.tesseract.lib.command.argument.OfflineUUIDPlayerArg;
import onl.tesseract.srp.service.player.SrpPlayerService;
import org.bukkit.command.CommandSender;

@Command(name = "illumination")
@org.springframework.stereotype.Component
public class IlluminationPointStaffCommand {
    private final SrpPlayerService playerService;

    public IlluminationPointStaffCommand(SrpPlayerService playerService) {
        this.playerService = playerService;
    }

    @Command(name = "get")
    public void get(CommandSender sender, @Argument("player") OfflineUUIDPlayerArg playerArg) {
        var player = playerService.getPlayer(playerArg.get().getUniqueId());
        sender.sendMessage(Component.text(player.getIlluminationPoints() + " points d'illumination"));
    }

    @Command(name = "give")
    public void give(@Argument("player") OfflineUUIDPlayerArg playerArg,
                     @Argument("amount") IntegerCommandArgument amountArg,
                     CommandSender sender) {
        boolean result = playerService.giveIlluminationPoints(playerArg.get().getUniqueId(), amountArg.get());
        if (result) {
            sender.sendMessage(Component.text( "Points donnés",NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text( "Échec",NamedTextColor.RED));
        }
    }

    @Command(name = "take")
    public void take(@Argument("player") OfflineUUIDPlayerArg playerArg,
                     @Argument("amount") IntegerCommandArgument amountArg,
                     CommandSender sender) {
        boolean result = playerService.giveIlluminationPoints(playerArg.get().getUniqueId(), -amountArg.get());
        if (result) {
            sender.sendMessage(Component.text( "Points retirés",NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text( "Échec",NamedTextColor.RED));
        }
    }
}


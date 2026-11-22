package onl.tesseract.srp.player.adapter.userside.controller.commands.staff;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.commandBuilder.annotation.Argument;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.lib.command.argument.IntegerCommandArgument;
import onl.tesseract.lib.command.argument.OfflineUUIDPlayerArg;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.player.domain.port.userside.PlayerIlluminationService;
import onl.tesseract.srp.player.domain.port.userside.SrpPlayerProfileService;
import org.bukkit.command.CommandSender;

@Command(name = "illumination")
@org.springframework.stereotype.Component
public class IlluminationPointStaffCommand {
    private final SrpPlayerProfileService srpPlayerProfileService;
    private final PlayerIlluminationService playerIlluminationService;

    public IlluminationPointStaffCommand(SrpPlayerProfileService srpPlayerProfileService, PlayerIlluminationService playerIlluminationService) {
        this.srpPlayerProfileService = srpPlayerProfileService;
        this.playerIlluminationService = playerIlluminationService;
    }

    @Command(name = "get")
    public void get(CommandSender sender, @Argument("player") OfflineUUIDPlayerArg playerArg) {
        var player = srpPlayerProfileService.getPlayer(new PlayerId(playerArg.get().getUniqueId()));
        sender.sendMessage(Component.text(player.getIlluminationPoints() + " points d'illumination"));
    }

    @Command(name = "give")
    public void give(@Argument("player") OfflineUUIDPlayerArg playerArg,
                     @Argument("amount") IntegerCommandArgument amountArg,
                     CommandSender sender) {
        if (amountArg.get() <= 0) {
            sender.sendMessage(Component.text("Le montant doit être strictement positif", NamedTextColor.RED));
            return;
        }
        playerIlluminationService.addIlluminationPoints(new PlayerId(playerArg.get().getUniqueId()), amountArg.get());
        sender.sendMessage(Component.text( "Points donnés",NamedTextColor.GREEN));
    }

    @Command(name = "take")
    public void take(@Argument("player") OfflineUUIDPlayerArg playerArg,
                     @Argument("amount") IntegerCommandArgument amountArg,
                     CommandSender sender) {
        if (amountArg.get() <= 0) {
            sender.sendMessage(Component.text("Le montant doit être strictement positif", NamedTextColor.RED));
            return;
        }
        boolean result = playerIlluminationService.removeIlluminationPoints(new PlayerId(playerArg.get().getUniqueId()), amountArg.get());
        if (result) {
            sender.sendMessage(Component.text( "Points retirés",NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text( "Le joueur n'a pas assez de points d'illumination",NamedTextColor.RED));
        }
    }
}


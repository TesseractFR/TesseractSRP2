package onl.tesseract.srp.controller.command.staff;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.commandBuilder.annotation.Argument;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.lib.command.argument.IntegerCommandArgument;
import onl.tesseract.lib.command.argument.OfflineUUIDPlayerArg;
import onl.tesseract.srp.service.player.SrpPlayerService;
import org.bukkit.command.CommandSender;

@Command(name = "money")
@org.springframework.stereotype.Component
public class MoneyStaffCommand {
    private final SrpPlayerService playerService;

    public MoneyStaffCommand(SrpPlayerService playerService) {
        this.playerService = playerService;
    }

    @Command(name = "get")
    public void get(CommandSender sender, @Argument("player") OfflineUUIDPlayerArg playerArg) {
        var player = playerService.getPlayer(playerArg.get().getUniqueId());
        sender.sendMessage(Component.text(player.getMoney() + " Lys"));
    }

    @Command(name = "give", description = "Donner de l'argent à un joueur")
    public void giveMoney(@Argument("player") OfflineUUIDPlayerArg playerArg, @Argument("amount") IntegerCommandArgument amountArg, CommandSender sender) {
        boolean result = playerService.giveMoneyAsStaff(playerArg.get().getUniqueId(), amountArg.get());
        if (result) {
            sender.sendMessage(Component.text( "Argent ajouté",NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text( "Le joueur n'a pas assez d'argent",NamedTextColor.RED));
        }
    }

    @Command(name = "take", description = "Retirer de l'argent à un joueur")
    public void takeMoney(@Argument("player") OfflineUUIDPlayerArg playerArg, @Argument("amount") IntegerCommandArgument amountArg, CommandSender sender) {
        boolean result = playerService.giveMoneyAsStaff(playerArg.get().getUniqueId(), -amountArg.get());
        if (result) {
            sender.sendMessage(Component.text( "Argent retiré",NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text( "Le joueur n'a pas assez d'argent",NamedTextColor.RED));
        }
    }
}


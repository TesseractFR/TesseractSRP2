package onl.tesseract.srp.controller.command.staff;

import net.kyori.adventure.text.Component;
import onl.tesseract.commandBuilder.annotation.Argument;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.lib.command.argument.OfflinePlayerArg;
import onl.tesseract.srp.controller.command.argument.PlayerRankArg;
import onl.tesseract.srp.service.player.SrpPlayerService;
import org.bukkit.command.CommandSender;

@Command(name = "rank")
@org.springframework.stereotype.Component
public class PlayerRankStaffCommand {
    private final SrpPlayerService playerService;

    public PlayerRankStaffCommand(SrpPlayerService playerService) {
        this.playerService = playerService;
    }

    @Command(name = "set")
    public void set(@Argument("player") OfflinePlayerArg playerArg, @Argument("rank") PlayerRankArg rankArg, CommandSender sender) {
        var player = playerArg.get();
        playerService.setRank(player.getUniqueId(), rankArg.get());
        sender.sendMessage(Component.text(player.getName() + " a maintenant le rang " + rankArg.get()));
    }
}


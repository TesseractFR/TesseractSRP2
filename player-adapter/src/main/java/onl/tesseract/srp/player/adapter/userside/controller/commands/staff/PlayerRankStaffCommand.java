package onl.tesseract.srp.player.adapter.userside.controller.commands.staff;

import net.kyori.adventure.text.Component;
import onl.tesseract.commandBuilder.annotation.Argument;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.lib.command.argument.OfflinePlayerArg;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.player.adapter.userside.controller.commands.args.PlayerRankArg;
import onl.tesseract.srp.player.domain.port.userside.PlayerRankService;
import org.bukkit.command.CommandSender;

@Command(name = "rank")
@org.springframework.stereotype.Component
public class PlayerRankStaffCommand {
    private final PlayerRankService playerRankService;

    public PlayerRankStaffCommand(PlayerRankService playerRankService) {
        this.playerRankService = playerRankService;
    }

    @Command(name = "set")
    public void set(@Argument("player") OfflinePlayerArg playerArg, @Argument("rank") PlayerRankArg rankArg, CommandSender sender) {
        var player = playerArg.get();
        playerRankService.changeRank(new PlayerId(player.getUniqueId()), rankArg.get());
        sender.sendMessage(Component.text(player.getName() + " a maintenant le rang " + rankArg.get()));
    }
}


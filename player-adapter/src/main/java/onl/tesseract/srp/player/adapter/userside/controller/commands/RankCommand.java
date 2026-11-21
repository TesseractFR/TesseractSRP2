package onl.tesseract.srp.player.adapter.userside.controller.commands;

import onl.tesseract.commandBuilder.CommandContext;
import onl.tesseract.commandBuilder.CommandInstanceProvider;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.commandBuilder.annotation.CommandBody;
import onl.tesseract.lib.profile.PlayerProfileService;
import onl.tesseract.lib.task.TaskScheduler;
import onl.tesseract.srp.player.adapter.userside.controller.menu.PlayerRankProgressMenu;
import onl.tesseract.srp.player.domain.port.userside.PlayerRankService;
import onl.tesseract.srp.player.domain.port.userside.SrpPlayerProfileService;
import org.bukkit.entity.Player;
import org.springframework.stereotype.Component;

@Component
@Command(name = "rank", playerOnly = true)
public class RankCommand extends CommandContext {
    private final SrpPlayerProfileService srpPlayerProfileService;
    private final PlayerRankService playerRankService;
    private final PlayerProfileService playerProfileService;
    private final TaskScheduler scheduler;

    public RankCommand(CommandInstanceProvider provider, SrpPlayerProfileService srpPlayerProfileService,
                       PlayerRankService playerRankService, PlayerProfileService playerProfileService,
                       TaskScheduler scheduler) {
        super(provider);
        this.srpPlayerProfileService = srpPlayerProfileService;
        this.playerRankService = playerRankService;
        this.playerProfileService = playerProfileService;
        this.scheduler = scheduler;
    }

    @CommandBody
    public void openMenu(Player sender) {
        new PlayerRankProgressMenu(sender.getUniqueId(), srpPlayerProfileService, playerRankService, playerProfileService,
                scheduler, null).open(sender);
    }
}


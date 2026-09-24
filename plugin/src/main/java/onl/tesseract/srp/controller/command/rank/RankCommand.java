package onl.tesseract.srp.controller.command.rank;

import onl.tesseract.commandBuilder.CommandContext;
import onl.tesseract.commandBuilder.CommandInstanceProvider;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.commandBuilder.annotation.CommandBody;
import onl.tesseract.lib.profile.PlayerProfileService;
import onl.tesseract.lib.task.TaskScheduler;
import onl.tesseract.srp.controller.menu.player.PlayerRankProgressMenu;
import onl.tesseract.srp.service.player.SrpPlayerService;
import org.bukkit.entity.Player;
import org.springframework.stereotype.Component;

@Component
@Command(name = "rank", playerOnly = true)
public class RankCommand extends CommandContext {
    private final SrpPlayerService playerService;
    private final PlayerProfileService playerProfileService;
    private final TaskScheduler scheduler;

    public RankCommand(CommandInstanceProvider provider, SrpPlayerService playerService,
                      PlayerProfileService playerProfileService, TaskScheduler scheduler) {
        super(provider);
        this.playerService = playerService;
        this.playerProfileService = playerProfileService;
        this.scheduler = scheduler;
    }

    @CommandBody
    public void openMenu(Player sender) {
        new PlayerRankProgressMenu(sender.getUniqueId(), playerService, playerProfileService, scheduler, null).open(sender);
    }
}


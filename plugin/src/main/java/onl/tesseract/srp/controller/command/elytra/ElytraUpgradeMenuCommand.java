package onl.tesseract.srp.controller.command.elytra;

import onl.tesseract.commandBuilder.CommandContext;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.lib.profile.PlayerProfileService;
import onl.tesseract.srp.common.adapter.SrpCommandInstanceProvider;
import onl.tesseract.srp.controller.menu.elytra.ElytraUpgradeMenu;
import onl.tesseract.srp.service.equipment.elytra.ElytraService;
import onl.tesseract.srp.player.domain.port.userside.SrpPlayerProfileService;
import org.bukkit.entity.Player;
import org.springframework.stereotype.Component;

@Component
@Command(name = "elytrasupgrade", playerOnly = true)
public class ElytraUpgradeMenuCommand extends CommandContext {
    private final PlayerProfileService playerProfileService;
    private final ElytraService elytraService;
    private final SrpPlayerProfileService srpPlayerProfileService;

    public ElytraUpgradeMenuCommand(SrpCommandInstanceProvider provider,
                                    PlayerProfileService playerProfileService,
                                    ElytraService elytraService,
                                    SrpPlayerProfileService srpPlayerProfileService) {
        super(provider);
        this.playerProfileService = playerProfileService;
        this.elytraService = elytraService;
        this.srpPlayerProfileService = srpPlayerProfileService;
    }

    @Command
    public void openMenu(Player sender) {
        new ElytraUpgradeMenu(
                sender.getUniqueId(),
                playerProfileService,
                elytraService,
                srpPlayerProfileService,
                null
        ).open(sender);
    }
}

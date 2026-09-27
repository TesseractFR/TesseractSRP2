package onl.tesseract.srp.controller.command.elytra;

import onl.tesseract.commandBuilder.CommandContext;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.lib.equipment.EquipmentService;
import onl.tesseract.lib.profile.PlayerProfileService;
import onl.tesseract.srp.SrpCommandInstanceProvider;
import onl.tesseract.srp.controller.menu.elytra.ElytraUpgradeMenu;
import onl.tesseract.srp.service.equipment.elytra.ElytraService;
import onl.tesseract.srp.service.player.SrpPlayerService;
import org.bukkit.entity.Player;
import org.springframework.stereotype.Component;

@Component
@Command(name = "elytrasupgrade", playerOnly = true)
public class ElytraUpgradeMenuCommand extends CommandContext {
    private final PlayerProfileService playerProfileService;
    private final ElytraService elytraService;
    private final SrpPlayerService srpPlayerService;

    public ElytraUpgradeMenuCommand(SrpCommandInstanceProvider provider,
                                    PlayerProfileService playerProfileService,
                                    ElytraService elytraService,
                                    SrpPlayerService srpPlayerService) {
        super(provider);
        this.playerProfileService = playerProfileService;
        this.elytraService = elytraService;
        this.srpPlayerService = srpPlayerService;
    }

    @Command
    public void openMenu(Player sender) {
        new ElytraUpgradeMenu(
                sender.getUniqueId(),
                playerProfileService,
                elytraService,
                srpPlayerService,
                null
        ).open(sender);
    }
}

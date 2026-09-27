package onl.tesseract.srp.controller.command.elytra;

import onl.tesseract.commandBuilder.CommandContext;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.lib.equipment.EquipmentService;
import onl.tesseract.lib.profile.PlayerProfileService;
import onl.tesseract.srp.SrpCommandInstanceProvider;
import onl.tesseract.srp.controller.menu.elytra.ElytraMenu;
import onl.tesseract.srp.service.equipment.elytra.ElytraService;
import onl.tesseract.srp.service.player.SrpPlayerService;
import org.bukkit.entity.Player;
import org.springframework.stereotype.Component;

@Component
@Command(name = "elytras", playerOnly = true)
public class ElytraMenuCommand extends CommandContext {
    private final ElytraService elytraService;
    private final PlayerProfileService playerProfileService;
    private final EquipmentService equipmentService;
    private final SrpPlayerService srpPlayerService;

    public ElytraMenuCommand(SrpCommandInstanceProvider provider,
                             ElytraService elytraService,
                             PlayerProfileService playerProfileService,
                             EquipmentService equipmentService,
                             SrpPlayerService srpPlayerService) {
        super(provider);
        this.elytraService = elytraService;
        this.playerProfileService = playerProfileService;
        this.equipmentService = equipmentService;
        this.srpPlayerService = srpPlayerService;
    }

    @Command
    public void openMenu(Player sender) {
        new ElytraMenu(
                sender,
                elytraService,
                playerProfileService,
                equipmentService,
                srpPlayerService
        ).open(sender);
    }
}

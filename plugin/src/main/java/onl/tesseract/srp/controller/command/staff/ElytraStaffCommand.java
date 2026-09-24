package onl.tesseract.srp.controller.command.staff;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.commandBuilder.annotation.Argument;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.srp.controller.command.argument.ElytraUpgradeArg;
import onl.tesseract.srp.controller.command.argument.ElytraUpgradeLevelArg;
import onl.tesseract.srp.service.equipment.elytra.ElytraService;
import org.bukkit.command.CommandSender;

@Command(name = "elytra")
@org.springframework.stereotype.Component
public class ElytraStaffCommand {
    private final ElytraService elytraService;

    public ElytraStaffCommand(ElytraService elytraService) {
        this.elytraService = elytraService;
    }

    @Command(name = "set")
    public void set(@Argument("player") onl.tesseract.lib.command.argument.PlayerArg playerArg,
                    @Argument("upgrade") ElytraUpgradeArg upgradeArg,
                    @Argument("level") ElytraUpgradeLevelArg levelArg,
                    CommandSender sender) {
        var player = playerArg.get();
        boolean result = elytraService.setUpgradeLevel(player.getUniqueId(), upgradeArg.get(), levelArg.get());
        if (result) {
            sender.sendMessage(Component.text("Niveau d'upgrade défini",NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("Échec de la définition du niveau", NamedTextColor.RED));
        }
    }
}


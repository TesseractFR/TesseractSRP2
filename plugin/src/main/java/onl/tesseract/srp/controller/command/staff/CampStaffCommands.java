package onl.tesseract.srp.controller.command.staff;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.commandBuilder.annotation.Argument;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.commandBuilder.annotation.Perm;
import onl.tesseract.lib.command.argument.PlayerArg;
import onl.tesseract.lib.menu.MenuService;
import onl.tesseract.srp.territory.adapter.userside.controller.commands.args.CampOwnerArg;
import onl.tesseract.srp.territory.adapter.userside.controller.commands.args.TrustedPlayerArg;
import onl.tesseract.srp.common.adapter.mapper.CoordinateMapper;
import onl.tesseract.srp.territory.domain.port.userside.campement.CampementBorderService;
import onl.tesseract.srp.territory.domain.port.userside.campement.CampementService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@org.springframework.stereotype.Component
@Command(name = "camp", permission = @Perm("staff"), playerOnly = true)
public class CampStaffCommands {

    private final CampementService campementService;
    private final CampementBorderService campementBorderService;
    private final MenuService menuService;

    public CampStaffCommands(CampementService campementService, CampementBorderService campementBorderService, MenuService menuService) {
        this.campementService = campementService;
        this.campementBorderService = campementBorderService;
        this.menuService = menuService;
    }

    @Command(name = "create", description = "Créer un campement pour un joueur")
    public void createCamp(Player sender, @Argument("joueur") PlayerArg targetName) {
        var target = targetName.get();
        campementService.createCampementAsStaff(sender.getUniqueId(), target.getUniqueId(),
                CoordinateMapper.toCoordinate(target.getLocation()));
    }

    @Command(name = "delete", description = "Supprimer le campement d'un joueur")
    public void deleteCamp(Player sender, @Argument("joueur") CampOwnerArg ownerName) {
        var owner = Bukkit.getOfflinePlayer(UUID.fromString(ownerName.get()));
        UUID staffId = sender.getUniqueId();
        UUID ownerId = owner.getUniqueId();
        if (!campementService.canDeleteCampementAsStaff(staffId, ownerId)) return;

        menuService.openConfirmationMenu(sender,
                Component.text("⚠ Es-tu sûr de vouloir supprimer le campement de " + owner.getName() + "?", NamedTextColor.RED),
                null,
                () -> {
                    campementService.deleteCampementAsStaff(staffId, ownerId);
                    campementBorderService.clearBorders(ownerId);
                });
    }

    @Command(name = "trust", description = "Ajouter un membre (target) dans le campement d'un joueur (owner)")
    public void trustPlayer(Player sender, @Argument("owner") CampOwnerArg ownerName, @Argument("target") PlayerArg targetName) {
        UUID owner = UUID.fromString(ownerName.get());
        campementService.trustAsStaff(sender.getUniqueId(), owner, targetName.get().getUniqueId());
    }

    @Command(name = "untrust", description = "Retirer un joueur de confiance (target) du campement d'un joueur (owner)")
    public void untrustPlayer(Player sender, @Argument("owner") CampOwnerArg ownerName, @Argument("target") TrustedPlayerArg targetName) {
        campementService.untrustAsStaff(sender.getUniqueId(),
                UUID.fromString(ownerName.get()), UUID.fromString(targetName.get()));
    }

    @Command(name = "getTrustedPlayers", description = "Obtenir la liste des joueurs de confiance d'un campement")
    public void getTrustedPlayers(Player sender, @Argument("owner") CampOwnerArg ownerName) {
        campementService.showTrustedPlayers(sender.getUniqueId(), UUID.fromString(ownerName.get()));
    }
}


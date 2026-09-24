package onl.tesseract.srp.controller.command.staff;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.commandBuilder.annotation.Argument;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.commandBuilder.annotation.Perm;
import onl.tesseract.lib.command.argument.PlayerArg;
import onl.tesseract.lib.menu.MenuService;
import onl.tesseract.srp.controller.command.argument.CampOwnerArg;
import onl.tesseract.srp.controller.command.argument.TrustedPlayerArg;
import onl.tesseract.srp.mapper.CoordinateMapper;
import onl.tesseract.srp.territory.domain.model.enums.result.CreationResult;
import onl.tesseract.srp.territory.domain.model.enums.result.TrustResult;
import onl.tesseract.srp.territory.domain.model.enums.result.UntrustResult;
import onl.tesseract.srp.territory.domain.port.userside.campement.CampementBorderService;
import onl.tesseract.srp.territory.domain.port.userside.campement.CampementService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

import static onl.tesseract.srp.util.SrpChatFormats.*;

@org.springframework.stereotype.Component
@Command(name = "camp", permission = @Perm("staff"), playerOnly = true)
public class CampStaffCommands {
    private static final Component NO_CAMPEMENT_MESSAGE = CAMPEMENT_CHAT_ERROR.append(Component.text("Ce joueur ne possède pas de campement."));

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
        UUID uuid = target.getUniqueId();
        var location = target.getLocation();
        var chunk = location.getChunk();
        String chunkKey = chunk.getX() + "," + chunk.getZ();
        CreationResult result = campementService.createCampement(uuid, CoordinateMapper.toCoordinate(location));

        Component message = null;
        switch (result) {
            case ALREADY_HAS_TERRITORY:
                message = CAMPEMENT_CHAT_ERROR.append(Component.text("Ce joueur possède déjà un campement."));
                break;
            case INVALID_WORLD:
                message = CAMPEMENT_CHAT_ERROR.append(Component.text("Tu ne peux pas créer de campement dans ce monde."));
                break;
            case NEAR_SPAWN:
                message = CAMPEMENT_CHAT_ERROR.append(Component.text("Trop proche du spawn pour créer un campement."));
                break;
            case NAME_TAKEN, NOT_ENOUGH_MONEY, RANK_TOO_LOW :
                message = null;
                break;
            case TOO_CLOSE_TO_OTHER_TERRITORY:
                message = CAMPEMENT_CHAT_ERROR.append(Component.text("Un autre territoire est trop proche d'ici."));
                break;
            case ON_OTHER_TERRITORY:
                message = CAMPEMENT_CHAT_ERROR.append(Component.text("Impossible de créer un campement ici, tu es sur un autre territoire."));
                break;
            case SUCCESS : {
                message = CAMPEMENT_CHAT_SUCCESS.append(Component.text("Campement créé pour " + target.getName() + " dans le chunk " + chunkKey + "."));
                target.sendMessage(CAMPEMENT_CHAT_SUCCESS.append(Component.text("Un administrateur t'a créé un campement dans le chunk " + chunkKey + ".")));
                break;
            }
            default : message = null;
        }
        if (message != null) {
            sender.sendMessage(message);
        }
    }

    @Command(name = "delete", description = "Supprimer le campement d'un joueur")
    public void deleteCamp(Player sender, @Argument("joueur") CampOwnerArg ownerName) {
        var owner = Bukkit.getOfflinePlayer(UUID.fromString(ownerName.get()));
        var campement = campementService.getCampementByOwner(sender.getUniqueId());
        if (campement == null) {
            sender.sendMessage(NO_CAMPEMENT_MESSAGE);
            return;
        }

        menuService.openConfirmationMenu(
                sender,
                Component.text("⚠ Es-tu sûr de vouloir supprimer le campement de " + owner.getName() + "?",NamedTextColor.RED),
                null,
                () -> {
                    campementService.deleteCampement(campement.getOwnerID());
                    if (owner.getPlayer() != null) {
                        campementBorderService.clearBorders(owner.getPlayer().getUniqueId());
                    }
                    sender.sendMessage(CAMPEMENT_CHAT_SUCCESS.append(Component.text("Le campement de " + owner.getName() + " a été supprimé avec succès.")));
                    if (owner.getPlayer() != null) {
                        owner.getPlayer().sendMessage(CAMPEMENT_CHAT_ERROR.append(Component.text("Ton campement a été supprimé par un administrateur.")));
                    }
                    return null;
                }
        );
    }

    @Command(name = "trust", description = "Ajouter un membre (target) dans le campement d'un joueur (owner)")
    public void trustPlayer(CommandSender sender, @Argument("owner") CampOwnerArg ownerName, @Argument("target") PlayerArg targetName) {
        var owner = Bukkit.getOfflinePlayer(UUID.fromString(ownerName.get()));
        var target = targetName.get();

        if (owner.getUniqueId().equals(target.getUniqueId())) {
            sender.sendMessage(CAMPEMENT_CHAT_FORMAT.append(Component.text("Impossible d'ajouter le propriétaire lui-même en tant que joueur de confiance.")));
            return;
        }

        TrustResult success = campementService.trust(owner.getUniqueId(), target.getUniqueId());
        switch (success) {
            case NOT_ALLOWED -> {}
            case SUCCESS -> {
                sender.sendMessage(CAMPEMENT_CHAT_SUCCESS.append(Component.text(target.getName() + " a été ajouté dans le campement de " + owner.getName() + ".")));
                target.sendMessage(CAMPEMENT_CHAT_SUCCESS.append(Component.text("Tu as été ajouté au campement de " + owner.getName() + " en tant que joueur de confiance.")));
            }
            case ALREADY_TRUST ->
                    sender.sendMessage(CAMPEMENT_CHAT_FORMAT.append(Component.text("Ce joueur est déjà dans la liste de confiance.")));
            case TERRITORY_NOT_FOUND -> sender.sendMessage(NO_CAMPEMENT_MESSAGE);
        }
    }

    @Command(name = "untrust", description = "Retirer un joueur de confiance (target) du campement d'un joueur (owner)")
    public void untrustPlayer(CommandSender sender, @Argument("owner") CampOwnerArg ownerName, @Argument("target") TrustedPlayerArg targetName) {
        var owner = Bukkit.getOfflinePlayer(UUID.fromString(ownerName.get()));
        var target = Bukkit.getOfflinePlayer(UUID.fromString(targetName.get()));

        var campement = campementService.getCampementByOwner(owner.getUniqueId());
        if (campement == null) {
            sender.sendMessage(CAMPEMENT_CHAT_ERROR.append(Component.text(owner.getName() + " ne possède pas de campement.")));
            return;
        }

        UntrustResult success = campementService.untrust(owner.getUniqueId(), target.getUniqueId());
        switch (success) {
            case NOT_ALLOWED -> {}
            case NOT_TRUST ->
                    sender.sendMessage(CAMPEMENT_CHAT_ERROR.append(Component.text("Ce joueur n'est pas dans la liste de confiance de ce campement.")));
            case SUCCESS ->
                    sender.sendMessage(CAMPEMENT_CHAT_SUCCESS.append(Component.text(target.getName() + " a été retiré du campement de " + owner.getName() + ".")));
            case TERRITORY_NOT_FOUND -> sender.sendMessage(NO_CAMPEMENT_MESSAGE);
        }
    }

    @Command(name = "getTrustedPlayers", description = "Obtenir la liste des joueurs de confiance d'un campement")
    public void getTrustedPlayers(CommandSender sender, @Argument("owner") CampOwnerArg ownerName) {
        var owner = Bukkit.getOfflinePlayer(UUID.fromString(ownerName.get()));
        var campement = campementService.getCampementByOwner(owner.getUniqueId());
        if (campement == null) {
            sender.sendMessage(CAMPEMENT_CHAT_FORMAT.append(Component.text(owner.getName() + " ne possède pas de campement.")));
            return;
        }

        List<String> trusted = campement.getTrusted().stream()
                .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                .filter(name -> name != null)
                .collect(Collectors.toList());

        if (trusted.isEmpty()) {
            sender.sendMessage(CAMPEMENT_CHAT_FORMAT.append(Component.text("Aucun joueur de confiance pour le campement de " + owner.getName() + ".")));
        } else {
            sender.sendMessage(CAMPEMENT_CHAT_FORMAT.append(Component.text("Joueurs de confiance du campement de " + owner.getName() + " : " + String.join(", ", trusted))));
        }
    }
}


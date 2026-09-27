package onl.tesseract.srp.controller.command.campement;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.commandBuilder.CommandContext;
import onl.tesseract.commandBuilder.annotation.Argument;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.lib.command.argument.PlayerArg;
import onl.tesseract.lib.menu.MenuService;
import onl.tesseract.srp.SrpCommandInstanceProvider;
import onl.tesseract.srp.controller.command.argument.CampOwnerArg;
import onl.tesseract.srp.controller.command.argument.TrustedPlayerArg;
import onl.tesseract.srp.common.adapter.mapper.CoordinateMapper;
import onl.tesseract.srp.common.adapter.mapper.ChunkCoordMapper;
import onl.tesseract.srp.common.adapter.mapper.LocationMapper;
import onl.tesseract.srp.service.TeleportationService;
import onl.tesseract.srp.service.equipment.annexionStick.AnnexionStickService;
import onl.tesseract.srp.territory.domain.model.enums.result.BorderResult;
import onl.tesseract.srp.territory.domain.model.enums.result.CreationResult;
import onl.tesseract.srp.territory.domain.model.enums.result.TrustResult;
import onl.tesseract.srp.territory.domain.model.enums.result.UntrustResult;
import onl.tesseract.srp.territory.domain.port.userside.campement.CampementBorderService;
import onl.tesseract.srp.territory.domain.port.userside.campement.CampementService;
import onl.tesseract.srp.util.equipment.annexionStick.CampementAnnexionStickInvocable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

import static onl.tesseract.srp.common.adapter.SrpChatFormats.*;

@Command(name = "campement", playerOnly = true)
public class CampementCommands extends CommandContext {
    private static final String CAMP_BORDER_COMMAND = "/campement border";
    private static final Component CAMPEMENT_BORDER_MESSAGE = Component.text("Visualise les bordures avec ")
        .append(Component.text(CAMP_BORDER_COMMAND, NamedTextColor.GOLD))
        .append(Component.text("."));
    private static final Component NO_CAMPEMENT_MESSAGE = CAMPEMENT_CHAT_ERROR
        .append(Component.text("Tu ne possèdes pas de campement. Crées-en un avec "))
        .append(Component.text("/campement create", NamedTextColor.GOLD));
    private static final String NOT_IN_CAMPEMENT_WORLD_MESSAGE =
        CAMPEMENT_CHAT_ERROR + "Tu n'es pas dans le bon monde, cette commande n'est utilisable que dans le monde des campements.";

    private final CampementService campementService;
    private final CampementBorderService campementBorderService;
    private final MenuService menuService;
    private final TeleportationService teleportService;
    private final AnnexionStickService annexionStickService;

    public CampementCommands(SrpCommandInstanceProvider provider, CampementService campementService,
                            CampementBorderService campementBorderService, MenuService menuService,
                            TeleportationService teleportService, AnnexionStickService annexionStickService) {
        super(provider);
        this.campementService = campementService;
        this.campementBorderService = campementBorderService;
        this.menuService = menuService;
        this.teleportService = teleportService;
        this.annexionStickService = annexionStickService;
    }

    @Command(name = "create", description = "Créer un nouveau campement.")
    public void createCampement(Player sender) {
        CreationResult result = campementService.createCampement(sender.getUniqueId(), CoordinateMapper.toCoordinate(sender.getLocation()));
        Component msg;
        switch (result) {
            case INVALID_WORLD -> msg = Component.text(NOT_IN_CAMPEMENT_WORLD_MESSAGE);
            case NEAR_SPAWN -> msg = CAMPEMENT_CHAT_ERROR.append(Component.text("Tu es trop proche du spawn pour créer un campement."));
            case TOO_CLOSE_TO_OTHER_TERRITORY -> msg = CAMPEMENT_CHAT_ERROR.append(Component.text("Tu es trop proche d'un autre campement, tu ne peux pas en créer un ici."));
            case ALREADY_HAS_TERRITORY -> msg = CAMPEMENT_CHAT_ERROR.append(Component.text("Tu possèdes déjà un campement."));
            case ON_OTHER_TERRITORY -> msg = CAMPEMENT_CHAT_ERROR.append(Component.text("Tu ne peux pas créer un campement ici, tu es sur un autre territoire."));
            case RANK_TOO_LOW, NAME_TAKEN, NOT_ENOUGH_MONEY -> {
                return;
            }
            case SUCCESS -> msg = CAMPEMENT_CHAT_SUCCESS.append(Component.text("Campement créé avec succès ! Tu peux désormais t'installer confortablement dans ce chunk ;)"));
            default -> msg = Component.text("");
        }
        sender.sendMessage(msg);
    }

    @Command(name = "delete", description = "Supprimer son campement.")
    public void deleteCampement(Player sender) {
        UUID playerID = sender.getUniqueId();
        var campement = campementService.getCampementByOwner(playerID);
        if (campement == null) {
            sender.sendMessage(NO_CAMPEMENT_MESSAGE);
            return;
        }
        menuService.openConfirmationMenu(sender, Component.text("⚠ Es-tu sûr de vouloir supprimer ton campement ?",NamedTextColor.RED) , null, () -> {
            campementService.deleteCampement(campement.getOwnerID());
            campementBorderService.clearBorders(sender.getUniqueId());
            sender.sendMessage(CAMPEMENT_CHAT_SUCCESS.append(Component.text("Ton campement a été supprimé avec succès.")));
            return null;
        });
    }

    @Command(name = "spawn", description = "Se téléporter au spawn de son campement (pas d'argument) ou de celui d'un autre joueur.")
    public void teleportToCampementSpawn(Player sender, @Argument(value = "joueur", optional = true) CampOwnerArg ownerName) {
        String targetName = ownerName != null ? ownerName.get() : null;
        if (targetName == null || targetName.equals(sender.getName())) {
            if (campementService.getCampementByOwner(sender.getUniqueId()) == null) {
                sender.sendMessage(NO_CAMPEMENT_MESSAGE);
                return;
            }
            var loc = campementService.getCampSpawn(sender.getUniqueId());
            if (loc == null) {
                sender.sendMessage(CAMPEMENT_CHAT_ERROR.append(Component.text("Aucun spawn défini pour ton campement.")));
                return;
            }
            teleportService.teleport(sender, LocationMapper.toLocation(loc), () ->
                sender.sendMessage(CAMPEMENT_CHAT_SUCCESS.append(Component.text("Tu as été téléporté à ton campement."))));
            return;
        }
        var target = Bukkit.getOfflinePlayer(targetName);
        var loc = campementService.getCampSpawn(target.getUniqueId());
        if (loc == null) {
            sender.sendMessage(CAMPEMENT_CHAT_ERROR.append(Component.text(target.getName() + " ne possède pas de campement.")));
            return;
        }
        teleportService.teleport(sender, LocationMapper.toLocation(loc), () ->
            sender.sendMessage(CAMPEMENT_CHAT_SUCCESS.append(Component.text("Tu as été téléporté au campement de " + target.getName() + "."))));
    }

    @Command(name = "setspawn", description = "Placer un nouveau point de spawn de campement.")
    public void setCampementSpawn(Player sender) {
        switch (campementService.setSpawnpoint(sender.getUniqueId(), CoordinateMapper.toCoordinate(sender.getLocation()))) {
            case SUCCESS -> sender.sendMessage(CAMPEMENT_CHAT_SUCCESS.append(Component.text("Nouveau point de spawn défini ici !")));
            case NOT_ALLOWED -> sender.sendMessage(CAMPEMENT_CHAT_ERROR.append(Component.text("Tu n'es pas autorisé à changer le point de spawn.")));
            case OUTSIDE_TERRITORY -> sender.sendMessage(CAMPEMENT_CHAT_ERROR.append(Component.text("Tu dois être dans un chunk de ton campement pour définir le spawn. ")).append(CAMPEMENT_BORDER_MESSAGE));
            case TERRITORY_NOT_FOUND -> sender.sendMessage(NO_CAMPEMENT_MESSAGE);
        }
    }

    @Command(name = "claim", description = "Annexer un chunk libre")
    public void claimChunk(Player sender) {
        switch (campementService.claimChunk(sender.getUniqueId(), ChunkCoordMapper.toChunkCoord(sender.getLocation()))) {
            case SUCCESS -> sender.sendMessage(CAMPEMENT_CHAT_SUCCESS.append(Component.text("Le chunk (" + sender.getChunk().getX() + ", " + sender.getChunk().getZ() + ") a été annexé avec succès.")));
            case ALREADY_OWNED -> sender.sendMessage(CAMPEMENT_CHAT_FORMAT.append(Component.text("Tu possèdes déjà ce chunk. ")).append(CAMPEMENT_BORDER_MESSAGE));
            case ALREADY_OTHER -> sender.sendMessage(CAMPEMENT_CHAT_ERROR.append(Component.text("Ce chunk appartient à un autre campement. ")).append(CAMPEMENT_BORDER_MESSAGE));
            case NOT_ADJACENT -> sender.sendMessage(CAMPEMENT_CHAT_ERROR.append(Component.text("Tu dois sélectionner un chunk collé à ton campement. ")).append(CAMPEMENT_BORDER_MESSAGE));
            case TOO_CLOSE -> sender.sendMessage(CAMPEMENT_CHAT_ERROR.append(Component.text("Tu ne peux pas annexer ce chunk, il est trop proche d'un autre campement. ")).append(CAMPEMENT_BORDER_MESSAGE));
            case NOT_ALLOWED -> sender.sendMessage(CAMPEMENT_CHAT_ERROR.append(Component.text("Tu n'es pas autorisé à annexer ce chunk.")));
            case TERRITORY_NOT_FOUND -> sender.sendMessage(NO_CAMPEMENT_MESSAGE);
            case INVALID_WORLD -> sender.sendMessage(CAMPEMENT_CHAT_ERROR.append(Component.text("Tu ne peux pas claim dans ce monde.")));
        }
    }

    @Command(name = "unclaim", description = "Désannexer un chunk de son campement.")
    public void unclaimChunk(Player sender) {
        switch (campementService.unclaimChunk(sender.getUniqueId(), ChunkCoordMapper.toChunkCoord(sender.getLocation()))) {
            case SUCCESS -> sender.sendMessage(CAMPEMENT_CHAT_SUCCESS.append(Component.text("Le chunk (" + sender.getChunk().getX() + ", " + sender.getChunk().getZ() + ") a été retiré de ton campement.")));
            case NOT_OWNED -> sender.sendMessage(CAMPEMENT_CHAT_ERROR.append(Component.text("Ce chunk ne fait pas partie de ton campement. ")).append(CAMPEMENT_BORDER_MESSAGE));
            case NOT_ALLOWED -> sender.sendMessage(CAMPEMENT_CHAT_ERROR.append(Component.text("Tu n'es pas autorisé(e) à utiliser cette commande.")));
            case LAST_CHUNK -> sender.sendMessage(CAMPEMENT_CHAT_ERROR.append(Component.text("Tu ne peux pas désannexer ton dernier chunk de campement.")));
            case IS_SPAWN_CHUNK -> sender.sendMessage(CAMPEMENT_CHAT_ERROR.append(Component.text("Tu ne peux pas désannexer ce chunk, il contient ton point de spawn.")));
            case TERRITORY_NOT_FOUND -> sender.sendMessage(NO_CAMPEMENT_MESSAGE);
            case SPLIT -> sender.sendMessage(CAMPEMENT_CHAT_ERROR.append(Component.text("Impossible de désannexer ce chunk, cela diviserait ton campement en 2 parties). ")).append(CAMPEMENT_BORDER_MESSAGE));
        }
    }

    @Command(name = "trust", description = "Ajouter un joueur de confiance dans son campement.")
    public void trustPlayer(Player sender, @Argument("joueur") PlayerArg targetPlayerArg) {
        UUID ownerID = sender.getUniqueId();
        UUID trustedPlayerID = targetPlayerArg.get().getUniqueId();

        if (ownerID.equals(trustedPlayerID)) {
            sender.sendMessage(CAMPEMENT_CHAT_FORMAT + "C'est bien, tu as confiance en toi ! Mais bon, t'es déjà propriétaire :)");
            return;
        }

        TrustResult success = campementService.trust(ownerID, trustedPlayerID);
        switch (success) {
            case NOT_ALLOWED -> sender.sendMessage(CAMPEMENT_CHAT_ERROR.append(Component.text("Impossible d'ajouter ce joueur. Assure-toi d'être le propriétaire du campement et que le joueur n'est pas déjà ajouté.")));
            case SUCCESS -> {
                sender.sendMessage(CAMPEMENT_CHAT_SUCCESS.append(Component.text(targetPlayerArg.get().getName() + " a été ajouté en tant que joueur de confiance dans ton campement !")));
                targetPlayerArg.get().sendMessage(CAMPEMENT_CHAT_SUCCESS.append(Component.text("Tu as été ajouté en tant que joueur de confiance dans le campement de " + sender.getName() + ".")));
            }
            case ALREADY_TRUST -> sender.sendMessage(CAMPEMENT_CHAT_FORMAT.append(Component.text("Ce joueur est déjà ajouté à ton campement.")));
            case TERRITORY_NOT_FOUND -> sender.sendMessage(NO_CAMPEMENT_MESSAGE);
        }
    }

    @Command(name = "untrust", description = "Retirer un joueur de confiance de son campement.")
    public void untrustPlayer(Player sender, @Argument("joueur") TrustedPlayerArg targetName) {
        UUID ownerID = sender.getUniqueId();
        var target = Bukkit.getOfflinePlayer(targetName.get());
        UUID trustedPlayerUUID = target.getUniqueId();

        UntrustResult success = campementService.untrust(ownerID, trustedPlayerUUID);
        switch (success) {
            case NOT_ALLOWED -> sender.sendMessage(CAMPEMENT_CHAT_ERROR.append(Component.text("Tu n'es pas autorisé à utiliser cette commande.")));
            case NOT_TRUST -> sender.sendMessage(CAMPEMENT_CHAT_ERROR.append(Component.text("Ce joueur n'est pas dans ta liste de confiance.")));
            case SUCCESS -> sender.sendMessage(CAMPEMENT_CHAT_SUCCESS.append(Component.text(target.getName() + " a été retiré de la liste des joueurs de confiance de ton campement !")));
            case TERRITORY_NOT_FOUND -> sender.sendMessage(NO_CAMPEMENT_MESSAGE);
        }
    }

    @Command(name = "border", description = "Afficher/Masquer les bordures de son campement.")
    public void toggleBorder(Player sender) {
        BorderResult result = campementBorderService.toggleBorders(sender.getUniqueId(), sender.getWorld().getName());
        Component msg = null;
        switch (result) {
            case SHOW_BORDERS -> msg = CAMPEMENT_CHAT_SUCCESS.append(Component.text("Les bordures de ton campement sont maintenant visibles !"));
            case CLEAR_BORDERS -> msg = CAMPEMENT_CHAT_FORMAT.append(Component.text("Les bordures de ton campement ont été masquées."));
            case TERRITORY_NOT_FOUND -> msg = NO_CAMPEMENT_MESSAGE;
            case INVALID_WORLD -> msg = Component.text(NOT_IN_CAMPEMENT_WORLD_MESSAGE);
        }
        sender.sendMessage(msg);
    }

    @Command(name = "stick", description = "Recevoir un Bâton d'annexion de campement.")
    public void giveCampementStick(Player sender) {
        annexionStickService.giveStick(sender.getUniqueId(), CampementAnnexionStickInvocable.class, CampementAnnexionStickInvocable::new);
    }
}


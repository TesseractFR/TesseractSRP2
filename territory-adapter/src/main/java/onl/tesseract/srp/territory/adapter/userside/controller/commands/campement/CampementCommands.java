package onl.tesseract.srp.territory.adapter.userside.controller.commands.campement;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.commandBuilder.CommandContext;
import onl.tesseract.commandBuilder.annotation.Argument;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.lib.command.argument.PlayerArg;
import onl.tesseract.lib.menu.MenuService;
import onl.tesseract.srp.common.adapter.SrpCommandInstanceProvider;
import onl.tesseract.srp.common.adapter.mapper.ChunkCoordMapper;
import onl.tesseract.srp.common.adapter.mapper.CoordinateMapper;
import onl.tesseract.srp.common.domain.model.Coordinate;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.port.userside.TeleportationService;
import onl.tesseract.srp.territory.adapter.userside.controller.annexionStick.AnnexionStickService;
import onl.tesseract.srp.territory.adapter.userside.controller.annexionStick.CampementAnnexionStickInvocable;
import onl.tesseract.srp.territory.adapter.userside.controller.commands.args.CampOwnerArg;
import onl.tesseract.srp.territory.adapter.userside.controller.commands.args.TrustedPlayerArg;
import onl.tesseract.srp.territory.adapter.userside.controller.menu.campement.CampementMenu;
import onl.tesseract.srp.territory.domain.port.userside.campement.CampementBorderService;
import onl.tesseract.srp.territory.domain.port.userside.campement.CampementService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@org.springframework.stereotype.Component
@Command(name = "campement", playerOnly = true)
public class CampementCommands extends CommandContext {

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
        campementService.createCampement(sender.getUniqueId(), CoordinateMapper.toCoordinate(sender.getLocation()));
    }

    @Command(name = "delete", description = "Supprimer son campement.")
    public void deleteCampement(Player sender) {
        UUID id = sender.getUniqueId();
        if (!campementService.hasCampement(id)) return;
        menuService.openConfirmationMenu(sender,
                Component.text("⚠ Es-tu sûr de vouloir supprimer ton campement ?", NamedTextColor.RED),
                null, () -> {
                    campementService.deleteOwnCampement(id);
                    campementBorderService.clearBorders(id);
                });
    }

    @Command(name = "menu", description = "Ouvrir le menu du campement.")
    public void openMenu(Player sender) {
        if (!campementService.hasCampement(sender.getUniqueId())) return;
        new CampementMenu(sender.getUniqueId(), campementService, menuService,
                teleportService, null).open(sender);
    }

    @Command(name = "spawn", description = "Se téléporter au spawn de son campement (pas d'argument) ou de celui d'un autre joueur.")
    public void teleportToCampementSpawn(Player sender, @Argument(value = "joueur", optional = true) CampOwnerArg ownerName) {
        UUID id = sender.getUniqueId();
        UUID targetOwner = ownerName != null ? Bukkit.getOfflinePlayer(ownerName.get()).getUniqueId() : null;
        Coordinate destination = campementService.resolveSpawnDestination(id, targetOwner);
        if (destination == null) return;
        teleportService.teleport(new PlayerId(id), destination,
                () -> campementService.notifyTeleported(id, targetOwner));
    }

    @Command(name = "setspawn", description = "Placer un nouveau point de spawn de campement.")
    public void setCampementSpawn(Player sender) {
        campementService.defineSpawn(sender.getUniqueId(), CoordinateMapper.toCoordinate(sender.getLocation()));
    }

    @Command(name = "claim", description = "Annexer un chunk libre")
    public void claimChunk(Player sender) {
        campementService.claim(sender.getUniqueId(), ChunkCoordMapper.toChunkCoord(sender.getLocation()));
    }

    @Command(name = "unclaim", description = "Désannexer un chunk de son campement.")
    public void unclaimChunk(Player sender) {
        campementService.unclaim(sender.getUniqueId(), ChunkCoordMapper.toChunkCoord(sender.getLocation()));
    }

    @Command(name = "trust", description = "Ajouter un joueur de confiance dans son campement.")
    public void trustPlayer(Player sender, @Argument("joueur") PlayerArg targetPlayerArg) {
        campementService.trustPlayer(sender.getUniqueId(), targetPlayerArg.get().getUniqueId());
    }

    @Command(name = "untrust", description = "Retirer un joueur de confiance de son campement.")
    public void untrustPlayer(Player sender, @Argument("joueur") TrustedPlayerArg targetName) {
        UUID target = Bukkit.getOfflinePlayer(targetName.get()).getUniqueId();
        campementService.untrustPlayer(sender.getUniqueId(), target);
    }

    @Command(name = "border", description = "Afficher/Masquer les bordures de son campement.")
    public void toggleBorder(Player sender) {
        campementBorderService.toggle(sender.getUniqueId(), sender.getWorld().getName());
    }

    @Command(name = "stick", description = "Recevoir un Bâton d'annexion de campement.")
    public void giveCampementStick(Player sender) {
        annexionStickService.giveStick(sender.getUniqueId(), CampementAnnexionStickInvocable.class, CampementAnnexionStickInvocable::new);
    }
}
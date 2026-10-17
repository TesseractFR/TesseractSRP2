package onl.tesseract.srp.territory.adapter.userside.controller.commands.guild;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.commandBuilder.CommandContext;
import onl.tesseract.commandBuilder.CommandInstanceProvider;
import onl.tesseract.commandBuilder.annotation.Argument;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.lib.chat.ChatEntryService;
import onl.tesseract.lib.command.argument.PlayerArg;
import onl.tesseract.lib.command.argument.StringArg;
import onl.tesseract.lib.menu.MenuService;
import onl.tesseract.srp.common.domain.model.Coordinate;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.port.userside.TeleportationService;
import onl.tesseract.srp.common.adapter.mapper.ChunkCoordMapper;
import onl.tesseract.srp.common.adapter.mapper.CoordinateMapper;
import onl.tesseract.srp.territory.adapter.userside.controller.annexionStick.AnnexionStickService;
import onl.tesseract.srp.territory.adapter.userside.controller.annexionStick.GuildAnnexionStickInvocable;
import onl.tesseract.srp.territory.adapter.userside.controller.commands.args.GuildArg;
import onl.tesseract.srp.territory.adapter.userside.controller.commands.args.GuildMembersArg;
import onl.tesseract.srp.territory.adapter.userside.controller.commands.args.GuildSpawnKindArg;
import onl.tesseract.srp.territory.adapter.userside.controller.menu.guild.GuildBaseMenu;
import onl.tesseract.srp.territory.adapter.userside.controller.menu.guild.GuildMenu;
import onl.tesseract.srp.territory.domain.model.guild.Guild;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildSpawnKind;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildBorderService;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@org.springframework.stereotype.Component
@Command(name = "guild")
public class GuildCommand extends CommandContext {

    private final GuildService guildService;
    private final GuildBorderService guildBorderService;
    private final ChatEntryService chatEntryService;
    private final MenuService menuService;
    private final TeleportationService teleportService;
    private final AnnexionStickService annexionStickService;

    public GuildCommand(CommandInstanceProvider provider, GuildService guildService, GuildBorderService guildBorderService,
                        ChatEntryService chatEntryService, MenuService menuService, TeleportationService teleportService,
                        AnnexionStickService annexionStickService) {
        super(provider);
        this.guildService = guildService;
        this.guildBorderService = guildBorderService;
        this.chatEntryService = chatEntryService;
        this.menuService = menuService;
        this.teleportService = teleportService;
        this.annexionStickService = annexionStickService;
    }

    @Command(name = "create", playerOnly = true, description = "Créer une nouvelle guilde")
    public void createGuild(Player sender, @Argument("nom") StringArg nameArg) {
        guildService.createGuild(sender.getUniqueId(),
                CoordinateMapper.toCoordinate(sender.getLocation()), nameArg.get());
    }

    @Command(name = "delete", playerOnly = true, description = "Supprimer sa guilde.")
    public void deleteGuild(Player sender) {
        UUID id = sender.getUniqueId();
        if (!guildService.canDeleteGuild(id)) return;
        menuService.openConfirmationMenu(sender,
                Component.text("⚠ Es-tu sûr de vouloir supprimer ta guilde ?", NamedTextColor.RED),
                null, () -> guildService.deleteGuild(id));
    }

    @Command(name = "menu", playerOnly = true, description = "Ouvrir le menu des guildes.")
    public void openMenu(Player sender) {
        if (!guildService.canOpenMenu(sender.getUniqueId())) return;
        new GuildMenu(sender.getUniqueId(), guildService, menuService, teleportService,
                chatEntryService, null).open(sender);
    }

    @Command(name = "invite", playerOnly = true, description = "Inviter un joueur dans sa guilde.")
    public void invite(Player sender, @Argument("joueur") PlayerArg player) {
        guildService.invitePlayer(sender.getUniqueId(), player.get().getUniqueId());
    }

    @Command(name = "join", playerOnly = true, description = "Demander à rejoindre une guilde.")
    public void join(Player sender,
                     @Argument("guilde") GuildArg guildArg,
                     @Argument(value = "message", optional = true) StringArg messageArg) {
        guildService.requestJoin(sender.getUniqueId(), guildArg.get().getName(),
                messageArg != null ? messageArg.get() : "");
    }

    @Command(name = "kick", playerOnly = true, description = "Exclure un membre de sa guilde.")
    public void kick(Player sender, @Argument("joueur") GuildMembersArg targetName) {
        UUID senderId = sender.getUniqueId();
        UUID targetId = Bukkit.getOfflinePlayer(targetName.get()).getUniqueId();
        if (!guildService.canKick(senderId, targetId)) return;
        menuService.openConfirmationMenu(sender,
                Component.text("⚠ Es-tu sûr de vouloir exclure ce joueur ?", NamedTextColor.RED),
                null, () -> guildService.kick(senderId, targetId));
    }

    @Command(name = "leave", playerOnly = true, description = "Quitter sa guilde.")
    public void leave(Player sender) {
        UUID id = sender.getUniqueId();
        if (!guildService.canLeave(id)) return;
        menuService.openConfirmationMenu(sender,
                Component.text("⚠ Es-tu sûr de vouloir quitter la guilde ?", NamedTextColor.RED),
                null, () -> guildService.leave(id));
    }

    @Command(name = "claim", playerOnly = true, description = "Annexer un chunk pour la guilde.")
    public void claimChunk(Player sender) {
        guildService.claim(sender.getUniqueId(), ChunkCoordMapper.toChunkCoord(sender.getLocation()));
    }

    @Command(name = "unclaim", playerOnly = true, description = "Retirer un chunk de la guilde.")
    public void unclaimChunk(Player sender) {
        guildService.unclaim(sender.getUniqueId(), ChunkCoordMapper.toChunkCoord(sender.getLocation()));
    }

    @Command(name = "border", playerOnly = true, description = "Afficher/Masquer les bordures de ta guilde.")
    public void toggleGuildBorder(Player sender) {
        guildBorderService.toggle(sender.getUniqueId(), sender.getWorld().getName());
    }

    @Command(name = "setspawn", playerOnly = true, description = "Définir le spawn privé (défaut) ou visiteurs.")
    public void setGuildSpawn(Player sender, @Argument(value = "type", optional = true) GuildSpawnKindArg kindArg) {
        GuildSpawnKind kind = kindArg != null ? kindArg.get() : GuildSpawnKind.PRIVATE;
        guildService.defineSpawn(sender.getUniqueId(),
                CoordinateMapper.toCoordinate(sender.getLocation()), kind);
    }

    @Command(name = "spawn", playerOnly = true, description = "Se téléporter au spawn de sa guilde (sans argument) ou d'une autre guilde.")
    public void teleportToGuildSpawn(Player sender, @Argument(value = "guilde", optional = true) GuildArg nameArg) {
        UUID id = sender.getUniqueId();
        Guild target = nameArg != null ? nameArg.get() : null;
        Coordinate destination = guildService.resolveSpawnDestination(id, target);
        if (destination == null) return;
        teleportService.teleport(new PlayerId(id), destination,
                () -> guildService.notifyTeleported(id, target));
    }

    @Command(name = "stick", description = "Recevoir un Bâton d'annexion de guilde.")
    public void giveGuildStick(Player sender) {
        annexionStickService.giveStick(sender.getUniqueId(), GuildAnnexionStickInvocable.class, GuildAnnexionStickInvocable::new);
    }
}
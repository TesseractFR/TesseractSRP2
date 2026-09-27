package onl.tesseract.srp.controller.command.guild;

import kotlin.Unit;
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
import onl.tesseract.srp.controller.command.argument.guild.GuildArg;
import onl.tesseract.srp.controller.command.argument.guild.GuildMembersArg;
import onl.tesseract.srp.controller.command.argument.guild.GuildSpawnKindArg;
import onl.tesseract.srp.controller.menu.guild.GuildMenu;
import onl.tesseract.srp.common.adapter.mapper.ChunkCoordMapper;
import onl.tesseract.srp.common.adapter.mapper.CoordinateMapper;
import onl.tesseract.srp.common.adapter.mapper.LocationMapper;
import onl.tesseract.srp.service.TeleportationService;
import onl.tesseract.srp.service.equipment.annexionStick.AnnexionStickService;
import onl.tesseract.srp.territory.domain.model.enums.result.BorderResult;
import onl.tesseract.srp.territory.domain.model.enums.result.CreationResult;
import onl.tesseract.srp.territory.domain.model.guild.Guild;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildRole;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildSpawnKind;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildBorderService;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildService;
import onl.tesseract.srp.util.equipment.annexionStick.GuildAnnexionStickInvocable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static onl.tesseract.srp.common.adapter.SrpChatFormats.*;

@org.springframework.stereotype.Component
@Command(name = "guild")
public class GuildCommand extends CommandContext {
    private static final Component NO_GUILD_MESSAGE = GUILD_CHAT_ERROR
        .append(Component.text("Tu ne possèdes pas de guilde. Rejoins ou crées-en une avec "))
        .append(Component.text("/guild create <nom>", NamedTextColor.GOLD));
    private static final String GUILD_BORDER_COMMAND = "/guild border";
    private static final Component GUILD_BORDER_MESSAGE = Component.text("Visualise les bordures avec ")
        .append(Component.text(GUILD_BORDER_COMMAND, NamedTextColor.GOLD))
        .append(Component.text("."));
    private static final String NOT_IN_GUILD_WORLD_MESSAGE =
        GUILD_CHAT_ERROR + "Tu n'es pas dans le bon monde, cette commande n'est utilisable que dans le monde des guildes.";

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
        CreationResult result = guildService.createGuild(sender.getUniqueId(), CoordinateMapper.toCoordinate(sender.getLocation()), nameArg.get());
        Component msg = null;
        switch (result) {
            case NOT_ENOUGH_MONEY -> msg = GUILD_CHAT_ERROR.append(Component.text("Tu n'as pas assez d'argent pour créer ta guilde."));
            case INVALID_WORLD -> msg = Component.text(NOT_IN_GUILD_WORLD_MESSAGE);
            case NEAR_SPAWN -> msg = GUILD_CHAT_ERROR.append(Component.text("Tu es trop proche du spawn pour créer ta guilde."));
            case TOO_CLOSE_TO_OTHER_TERRITORY -> msg = GUILD_CHAT_ERROR.append(Component.text("Impossible de créer ta guilde ici, tu es trop proche d'une autre guilde."));
            case NAME_TAKEN -> msg = GUILD_CHAT_ERROR.append(Component.text("Ce nom de guilde est déjà pris, choisis-en un autre."));
            case ALREADY_HAS_TERRITORY -> msg = GUILD_CHAT_ERROR.append(Component.text("Tu es déjà dans une guilde. Quitte-la pour pouvoir en créer une nouvelle."));
            case RANK_TOO_LOW -> msg = GUILD_CHAT_ERROR.append(Component.text("Tu n'as pas le grade minimal nécessaire pour créer une guilde (Baron)."));
            case ON_OTHER_TERRITORY -> msg = GUILD_CHAT_ERROR.append(Component.text("Tu ne peux pas créer une guilde ici, tu es sur un autre territoire."));
            case SUCCESS -> msg = GUILD_CHAT_SUCCESS.append(Component.text("Nouvelle guilde créée sous le nom de " + nameArg.get()));
        }
        sender.sendMessage(msg);
    }

    @Command(name = "delete", playerOnly = true, description = "Supprimer sa guilde.")
    public void deleteGuild(Player sender) {
        var role = guildService.getMemberRole(sender.getUniqueId());
        if (role == null) {
            sender.sendMessage(NO_GUILD_MESSAGE);
            return;
        }
        if (role != GuildRole.Leader) {
            sender.sendMessage(GUILD_CHAT_ERROR.append(Component.text("Tu n'as pas l'autorisation pour supprimer ta guilde.")));
            return;
        }
        menuService.openConfirmationMenu(sender, Component.text("⚠ Es-tu sûr de vouloir supprimer ta guilde ?",NamedTextColor.RED), null, () -> {
            boolean ok = guildService.deleteGuildAsLeader(sender.getUniqueId());
            if (ok) {
                sender.sendMessage(GUILD_CHAT_SUCCESS.append(Component.text("Ta guilde a été supprimée avec succès.")));
            } else {
                sender.sendMessage(GUILD_CHAT_ERROR.append(Component.text("Suppression impossible.")));
            }
            return null;
        });
    }

    @Command(name = "menu", playerOnly = true, description = "Ouvrir le menu des guildes.")
    public void openMenu(Player sender) {
        var guild = guildService.getGuildByLeader(sender.getUniqueId());
        if (guild == null) {
            sender.sendMessage(NO_GUILD_MESSAGE);
            return;
        }
        new GuildMenu(sender.getUniqueId(), guildService, chatEntryService).open(sender);
    }

    @Command(name = "invite", playerOnly = true, description = "Inviter un joueur dans sa guilde.")
    public void invite(Player sender, @Argument("joueur") PlayerArg player) {
        var target = player.get();
        switch (guildService.invite(sender.getUniqueId(), target.getUniqueId())) {
            case TERRITORY_NOT_FOUND -> sender.sendMessage(NO_GUILD_MESSAGE);
            case NOT_ALLOWED -> sender.sendMessage(GUILD_CHAT_ERROR.append(Component.text("Tu n'es pas autorisé à utiliser cette commande.")));
            case SAME_PLAYER -> sender.sendMessage(GUILD_CHAT_ERROR.append(Component.text("Tu ne peux pas t'inviter toi même.")));
            case HAS_GUILD -> sender.sendMessage(GUILD_CHAT_ERROR.append(Component.text("Ce joueur est déjà dans une guilde.")));
            case SUCCESS_JOIN -> {
                var senderGuild = guildService.getGuildByMember(sender.getUniqueId());
                sender.sendMessage(GUILD_CHAT_SUCCESS.append(Component.text(target.getName() + " a rejoint votre guilde.")));
                target.sendMessage(GUILD_CHAT_SUCCESS.append(Component.text("Vous avez rejoint la guilde " + senderGuild.getName() + ".")));
            }
            case SUCCESS_INVITE -> sender.sendMessage(GUILD_CHAT_FORMAT.append(Component.text("Votre invitation a bien été envoyée à " + target.getName())));
        }
    }

    @Command(name = "kick", playerOnly = true, description = "Exclure un membre de sa guilde.")
    public void kick(Player sender, @Argument("joueur") GuildMembersArg targetName) {
        var target = Bukkit.getOfflinePlayer(targetName.get());
        switch (guildService.kickMember(sender.getUniqueId(), target.getUniqueId())) {
            case TERRITORY_NOT_FOUND -> sender.sendMessage(NO_GUILD_MESSAGE);
            case NOT_MEMBER -> sender.sendMessage(GUILD_CHAT_ERROR.append(Component.text(target.getName() + " n'est pas membre de ta guilde.")));
            case NOT_ALLOWED -> sender.sendMessage(GUILD_CHAT_ERROR.append(Component.text("Tu n'es pas autorisé à exclure des membres.")));
            case CANNOT_KICK_LEADER -> sender.sendMessage(GUILD_CHAT_ERROR.append(Component.text("Tu ne peux pas exclure le chef de la guilde.")));
            case SUCCESS -> menuService.openConfirmationMenu(sender, Component.text("⚠ Es-tu sûr de vouloir quitter la guilde ?").color(NamedTextColor.RED), null, () ->{
                sender.sendMessage(GUILD_CHAT_SUCCESS.append(Component.text("Tu as quitté ta guilde.")));
                return Unit.INSTANCE;
            });
        }
    }

    @Command(name = "leave", playerOnly = true, description = "Quitter sa guilde.")
    public void leave(Player sender) {
        switch (guildService.leaveGuild(sender.getUniqueId())) {
            case TERRITORY_NOT_FOUND -> sender.sendMessage(NO_GUILD_MESSAGE);
            case LEADER_MUST_DELETE -> sender.sendMessage(GUILD_CHAT_ERROR.append(Component.text("Tu es le/la chef(fe) de la guilde. Supprime-la ou transfère le leadership.")));
            case SUCCESS -> menuService.openConfirmationMenu(sender, Component.text("⚠ Es-tu sûr de vouloir quitter la guilde ?").color(NamedTextColor.RED), null, () ->{
                sender.sendMessage(GUILD_CHAT_SUCCESS.append(Component.text("Tu as quitté ta guilde.")));
                return Unit.INSTANCE;
            });

        }
    }

    @Command(name = "claim", playerOnly = true, description = "Annexer un chunk pour la guilde.")
    public void claimChunk(Player sender) {
        switch (guildService.claimChunk(sender.getUniqueId(), ChunkCoordMapper.toChunkCoord(sender.getLocation()))) {
            case TERRITORY_NOT_FOUND -> sender.sendMessage(NO_GUILD_MESSAGE);
            case SUCCESS -> sender.sendMessage(GUILD_CHAT_SUCCESS.append(Component.text("Le chunk (" + sender.getChunk().getX() + ", " + sender.getChunk().getZ() + ") a été annexé avec succès pour la guilde.")));
            case ALREADY_OWNED -> sender.sendMessage(GUILD_CHAT_FORMAT.append(Component.text("Ta guilde possède déjà ce chunk. ")).append(GUILD_BORDER_MESSAGE));
            case ALREADY_OTHER -> sender.sendMessage(GUILD_CHAT_ERROR.append(Component.text("Ce chunk appartient à une autre guilde. ")).append(GUILD_BORDER_MESSAGE));
            case NOT_ADJACENT -> sender.sendMessage(GUILD_CHAT_ERROR.append(Component.text("Tu dois sélectionner un chunk collé au territoire de ta guilde. ")).append(GUILD_BORDER_MESSAGE));
            case NOT_ALLOWED -> sender.sendMessage(GUILD_CHAT_ERROR.append(Component.text("Tu n'es pas autorisé(e) à utiliser cette commande.")));
            case TOO_CLOSE -> sender.sendMessage(GUILD_CHAT_ERROR.append(Component.text("Tu ne peux pas annexer ce chunk, il est trop proche d'une autre guilde.")));
            case INVALID_WORLD -> sender.sendMessage(Component.text(NOT_IN_GUILD_WORLD_MESSAGE));
        }
    }

    @Command(name = "unclaim", playerOnly = true, description = "Retirer un chunk de la guilde.")
    public void unclaimChunk(Player sender) {
        switch (guildService.unclaimChunk(sender.getUniqueId(), ChunkCoordMapper.toChunkCoord(sender.getLocation()))) {
            case SUCCESS -> sender.sendMessage(GUILD_CHAT_SUCCESS.append(Component.text("Le chunk (" + sender.getChunk().getX() + ", " + sender.getChunk().getZ() + ") a été retiré de ta guilde.")));
            case NOT_OWNED -> sender.sendMessage(GUILD_CHAT_ERROR.append(Component.text("Ce chunk ne fait pas partie du territoire de ta guilde.")));
            case LAST_CHUNK -> sender.sendMessage(GUILD_CHAT_ERROR.append(Component.text("Tu ne peux pas retirer le dernier chunk de ta guilde ! Si tu veux supprimer ta guilde, utilise /guild delete.")));
            case IS_SPAWN_CHUNK -> sender.sendMessage(GUILD_CHAT_ERROR.append(Component.text("Tu ne peux pas désannexer ce chunk, il contient un point de spawn de ta guilde. Déplace-le dans un autre chunk avec /guild setspawn (private/visitor) avant de retirer celui-ci.")));
            case SPLIT -> sender.sendMessage(GUILD_CHAT_ERROR.append(Component.text("Tu ne peux pas désannexer ce chunk, cela diviserait ta guilde en plusieurs parties. ")).append(GUILD_BORDER_MESSAGE));
            case NOT_ALLOWED -> sender.sendMessage(GUILD_CHAT_ERROR.append(Component.text("Tu n'es pas autorisé(e) à utiliser cette commande.")));
            case TERRITORY_NOT_FOUND -> sender.sendMessage(NO_GUILD_MESSAGE);
        }
    }

    @Command(name = "border", playerOnly = true, description = "Afficher/Masquer les bordures de ta guilde.")
    public void toggleGuildBorder(Player sender) {
        BorderResult result = guildBorderService.toggleBorders(sender.getUniqueId(), sender.getWorld().getName());
        Component msg = null;
        switch (result) {
            case SHOW_BORDERS -> msg = GUILD_CHAT_SUCCESS.append(Component.text("Les bordures de ta guilde sont maintenant visibles !"));
            case CLEAR_BORDERS -> msg = GUILD_CHAT_SUCCESS.append(Component.text("Les bordures de ta guilde ont été masquées."));
            case TERRITORY_NOT_FOUND -> msg = NO_GUILD_MESSAGE;
            case INVALID_WORLD -> msg = Component.text(NOT_IN_GUILD_WORLD_MESSAGE);
        }
        sender.sendMessage(msg);
    }

    @Command(name = "setspawn", playerOnly = true, description = "Définir le spawn privé (défaut) ou visiteurs.")
    public void setGuildSpawn(Player sender, @Argument(value = "type", optional = true) GuildSpawnKindArg kindArg) {
        GuildSpawnKind kind = kindArg != null ? kindArg.get() : GuildSpawnKind.PRIVATE;
        switch (guildService.setSpawnpoint(sender.getUniqueId(), CoordinateMapper.toCoordinate(sender.getLocation()), kind)) {
            case SUCCESS -> {
                String label = kind == GuildSpawnKind.PRIVATE ? "privé" : "visiteurs";
                sender.sendMessage(GUILD_CHAT_SUCCESS.append(Component.text("Le point de spawn " + label + " de la guilde a été défini ici.")));
            }
            case NOT_ALLOWED -> sender.sendMessage(GUILD_CHAT_ERROR.append(Component.text("Tu n'as pas l'autorisation de changer le spawn.")));
            case OUTSIDE_TERRITORY -> sender.sendMessage(GUILD_CHAT_ERROR.append(Component.text("Tu dois être dans un chunk de ta guilde pour définir le spawn. ")).append(GUILD_BORDER_MESSAGE));
            case TERRITORY_NOT_FOUND -> sender.sendMessage(NO_GUILD_MESSAGE);
        }
    }

    @Command(name = "spawn", playerOnly = true, description = "Se téléporter au spawn de sa guilde (sans argument) ou d'une autre guilde.")
    public void teleportToGuildSpawn(Player sender, @Argument(value = "guilde", optional = true) GuildArg nameArg) {
        String targetName = nameArg != null ? nameArg.get().getName() : null;
        guildService.getGuildByMember(sender.getUniqueId());
        Guild guild;
        Component errorMsg = NO_GUILD_MESSAGE;

        if (targetName != null) {
            guild = guildService.getByName(targetName);
            errorMsg = GUILD_CHAT_ERROR.append(Component.text("La guilde \"" + targetName + "\" n'existe pas."));
        }
        else {
            guild = guildService.getGuildByMember(sender.getUniqueId());
        }

        if (guild == null) {
            sender.sendMessage(errorMsg);
            return;
        }

        var destination = targetName == null
            ? guildService.getPrivateSpawn(guild.getId())
            : (guildService.getVisitorSpawn(guild.getId()) != null ? guildService.getVisitorSpawn(guild.getId()) : guildService.getPrivateSpawn(guild.getId()));

        if (destination == null) {
            sender.sendMessage(GUILD_CHAT_ERROR.append(Component.text("Aucun spawn défini pour cette guilde.")));
            return;
        }

        teleportService.teleport(sender, LocationMapper.toLocation(destination), () -> {
            Component msg = targetName == null
                ? GUILD_CHAT_SUCCESS.append(Component.text("Tu as été téléporté au spawn de ta guilde."))
                : GUILD_CHAT_SUCCESS.append(Component.text("Tu as été téléporté au spawn de la guilde " + guild.getName() + "."));
            sender.sendMessage(msg);
        });
    }

    @Command(name = "stick", description = "Recevoir un Bâton d'annexion de guilde.")
    public void giveGuildStick(Player sender) {
        annexionStickService.giveStick(sender.getUniqueId(), GuildAnnexionStickInvocable.class, GuildAnnexionStickInvocable::new);
    }
}


package onl.tesseract.srp.controller.command.staff;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.commandBuilder.annotation.Argument;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.lib.command.argument.IntegerCommandArgument;
import onl.tesseract.lib.command.argument.PlayerArg;
import onl.tesseract.lib.menu.MenuService;
import onl.tesseract.srp.controller.command.argument.guild.GuildArg;
import onl.tesseract.srp.controller.command.argument.guild.GuildMembersRoleArg;
import onl.tesseract.srp.controller.command.argument.guild.GuildMembersArg;
import onl.tesseract.srp.controller.command.argument.guild.GuildRankArg;
import onl.tesseract.srp.common.domain.model.enums.StaffSetRoleResult;
import onl.tesseract.srp.territory.domain.model.enums.result.KickResult;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.Optional;

import static onl.tesseract.srp.common.adapter.SrpChatFormats.STAFF_CHAT_ERROR;
import static onl.tesseract.srp.common.adapter.SrpChatFormats.STAFF_CHAT_FORMAT;
import static onl.tesseract.srp.common.adapter.SrpChatFormats.STAFF_CHAT_SUCCESS;

@org.springframework.stereotype.Component
@Command(name = "guild")
public class GuildStaffCommand {
    private final GuildService guildService;
    private final MenuService menuService;

    public GuildStaffCommand(GuildService guildService, MenuService menuService) {
        this.guildService = guildService;
        this.menuService = menuService;
    }

    @Command(name = "delete", description = "Supprimer une guilde")
    public void deleteGuild(@Argument("guild") GuildArg guildArg, Player sender) {
        menuService.openConfirmationMenu(
            sender,
            Component.text("⚠ Es-tu sûr de vouloir supprimer la guilde " + guildArg.get().getName() + "?",NamedTextColor.RED),
            null,
            () -> {
                boolean ok = guildService.deleteGuildAsStaff(guildArg.get().getId());
                if (ok) {
                    sender.sendMessage(STAFF_CHAT_ERROR.append(Component.text("La guilde " + guildArg.get().getName() + " a été supprimée avec succès.")));
                } else {
                    sender.sendMessage(STAFF_CHAT_ERROR.append(Component.text("Suppression impossible.")));
                }
                return null;
            }
        );
    }

    @Command(name = "members", description = "Gérer les membres d'une guilde")
    @org.springframework.stereotype.Component
    public static class MembersCommand {
        private final GuildService guildService;

        public MembersCommand(GuildService guildService) {
            this.guildService = guildService;
        }

        @Command(name = "get", description = "Voir les membres d'une guilde")
        public void getMembers(@Argument("guild") GuildArg guildArg, Player sender) {
            var guild = guildArg.get();
            sender.sendMessage(STAFF_CHAT_FORMAT.append(Component.text("Membres de la guilde " + guild.getName() + ":")));
            for (var member : guild.getMembers()) {
                String playerName = Bukkit.getOfflinePlayer(member.getPlayerID()).getName() != null
                    ? Bukkit.getOfflinePlayer(member.getPlayerID()).getName()
                    : member.getPlayerID().toString();
                sender.sendMessage(STAFF_CHAT_FORMAT.append(Component.text("- " + playerName + " : " + member.getRole())));
            }
        }

        @Command(name = "add", description = "Ajouter un membre à une guilde")
        public void addMember(@Argument("guild") GuildArg guildArg, @Argument("playerName") PlayerArg playerArg, CommandSender sender) {
            var player = playerArg.get();
            // Check if player is already in guild
            boolean alreadyInGuild = guildArg.get().getMembers().stream()
                .anyMatch(m -> Bukkit.getOfflinePlayer(m.getPlayerID()).getUniqueId().equals(player.getUniqueId()));

            if (alreadyInGuild) {
                sender.sendMessage(STAFF_CHAT_ERROR.append(Component.text("Le joueur " + player.getName() + " fait déjà partie de la guilde.")));
                return;
            }

            guildService.addMemberAsStaff(guildArg.get().getId(), player.getUniqueId());
            sender.sendMessage(STAFF_CHAT_SUCCESS.append(Component.text("Opération effectuée - " + playerArg.get().getName() + " ajouté à la guilde " + guildArg.get().getName())));
        }

        @Command(name = "kick", description = "Expulser un joueur d'une guilde")
        public void kickMember(@Argument("guild") GuildArg guildArg, @Argument("playerName") GuildMembersArg playerArg, CommandSender sender) {
            var guild = guildArg.get();
            var player = Bukkit.getOfflinePlayer(UUID.fromString(playerArg.get()));
            KickResult result = guildService.kickMember(guild.getLeaderId(), player.getUniqueId());

            switch (result) {
                case TERRITORY_NOT_FOUND ->
                    sender.sendMessage(STAFF_CHAT_ERROR.append(Component.text("Le joueur " + player.getName() + " ne fait partie d'aucune guilde.")));
                case NOT_MEMBER ->
                    sender.sendMessage(STAFF_CHAT_ERROR.append(Component.text("Le joueur " + player.getName() + " ne fait pas partie de la guilde.")));
                case CANNOT_KICK_LEADER ->
                    sender.sendMessage(STAFF_CHAT_ERROR.append(Component.text("Impossible d'expulser le leader de la guilde. Change son rôle.")));
                case NOT_ALLOWED ->
                    sender.sendMessage(STAFF_CHAT_ERROR.append(Component.text("Tu n'es pas autorisé à expulser ce joueur.")));
                case SUCCESS ->
                    sender.sendMessage(STAFF_CHAT_SUCCESS.append(Component.text("Opération effectuée - " + player.getName() + " expulsé de la guilde " + guild.getName())));
            }
        }

        @Command(name = "setRole", description = "Définir le rôle d'un joueur dans une guilde. Si on rétrograde le leader actuel, indiquer [newLeader].")
        public void setRole(CommandSender sender, @Argument("guild") GuildArg guildArg, @Argument("player") GuildMembersArg playerArg,
                           @Argument("role") GuildMembersRoleArg roleArg, @Argument(value = "newLeader", optional = true) GuildMembersArg newLeaderArg) {
            var guild = guildArg.get();
            String playerName = playerArg.get();
            var role = roleArg.get();

            Optional<OfflinePlayer> targetOpt = guild.getMembers().stream()
                .map(m -> Bukkit.getOfflinePlayer(m.getPlayerID()))
                .filter(it -> {
                    String name = it.getName();
                    return name != null && name.equalsIgnoreCase(playerName) || it.getUniqueId().toString().equals(playerName);
                })
                .findFirst();

            if (targetOpt.isEmpty()) {
                sender.sendMessage(STAFF_CHAT_ERROR.append(Component.text("Joueur \"" + playerName + "\" introuvable.")));
                return;
            }

            var target = targetOpt.get();
            var oldLeader = Bukkit.getOfflinePlayer(guild.getLeaderId());
            UUID replacementLeaderID = newLeaderArg != null ? Bukkit.getOfflinePlayer(newLeaderArg.get()).getUniqueId() : null;

            StaffSetRoleResult result = guildService.setMemberRoleAsStaff(guild.getId(), target.getUniqueId(), role, replacementLeaderID);

            switch (result) {
                case SUCCESS -> {
                    if (role.name().equalsIgnoreCase("Leader")) {
                        sender.sendMessage(STAFF_CHAT_SUCCESS.append(Component.text("Leader de la guilde " + guild.getName() + " défini sur " + target.getName() + ". L'ancien leader " + oldLeader.getName() + " a été passé Citoyen.")));
                    } else if (replacementLeaderID != null) {
                        var replacementLeader = Bukkit.getOfflinePlayer(replacementLeaderID);
                        sender.sendMessage(STAFF_CHAT_SUCCESS.append(Component.text("Rôle de " + target.getName() + " défini à " + role.name() + " et " + replacementLeader.getName() + " à Leader pour la guilde " + guild.getName() + ".")));
                    }
                }
                case NEED_NEW_LEADER ->
                    sender.sendMessage(STAFF_CHAT_ERROR.append(Component.text("La cible est le leader actuel. Indique un nouveau leader : /staffrole set " + guild.getName() + " " + playerName + " " + role.name() + " <newLeader>")));
                case NEW_LEADER_SAME_AS_TARGET ->
                    sender.sendMessage(STAFF_CHAT_ERROR.append(Component.text("Le nouveau leader ne peut pas être le même joueur que la cible.")));
                case SAME_ROLE ->
                    sender.sendMessage(STAFF_CHAT_ERROR.append(Component.text((target.getName() != null ? target.getName() : target.getUniqueId().toString()) + " a déjà le rôle " + role.name() + " dans la guilde " + guild.getName() + ".")));
            }
        }
    }

    @Command(name = "money", description = "Gérer l'argent d'une guilde")
    @org.springframework.stereotype.Component
    public static class MoneyCommand {
        private final GuildService guildService;

        public MoneyCommand(GuildService guildService) {
            this.guildService = guildService;
        }

        @Command(name = "give")
        public void giveMoney(@Argument("guild") GuildArg guildArg, @Argument("amount") IntegerCommandArgument amountArg, CommandSender sender) {
            guildService.giveMoneyAsStaff(guildArg.get().getId(), amountArg.get());
            sender.sendMessage(STAFF_CHAT_SUCCESS.append(Component.text("Opération effectuée")));
        }

        @Command(name = "take")
        public void takeMoney(@Argument("guild") GuildArg guildArg, @Argument("amount") IntegerCommandArgument amountArg, CommandSender sender) {
            guildService.giveMoneyAsStaff(guildArg.get().getId(), -amountArg.get());
            sender.sendMessage(STAFF_CHAT_SUCCESS.append(Component.text("Opération effectuée")));
        }

        @Command(name = "get")
        public void getMoney(@Argument("guild") GuildArg guildArg, CommandSender sender) {
            var guild = guildArg.get();
            sender.sendMessage(STAFF_CHAT_FORMAT.append(Component.text(guild.getName() + " : " + guild.getMoney() + " Lys")));
        }
    }

    @Command(name = "level", description = "Gérer le niveau d'une guilde")
    @org.springframework.stereotype.Component
    public static class LevelCommand {
        private final GuildService guildService;

        public LevelCommand(GuildService guildService) {
            this.guildService = guildService;
        }

        @Command(name = "set")
        public void setLevel(@Argument("guild") GuildArg guildArg, @Argument("level") IntegerCommandArgument levelArg, CommandSender sender) {
            guildService.setLevel(guildArg.get().getId(), levelArg.get());
            sender.sendMessage(STAFF_CHAT_SUCCESS.append(Component.text("Opération effectuée - niveau de " + guildArg.get().getName() + " défini à " + levelArg.get())));
        }

        @Command(name = "get")
        public void getLevel(@Argument("guild") GuildArg guildArg, CommandSender sender) {
            var guild = guildArg.get();
            sender.sendMessage(STAFF_CHAT_FORMAT.append(Component.text(guild.getName() + " : niveau " + guild.getLevel())));
        }

        @Command(name = "addLevel")
        public void addLevel(@Argument("guild") GuildArg guildArg, @Argument("amount") IntegerCommandArgument levelArg, CommandSender sender) {
            guildService.addLevel(guildArg.get().getId(), levelArg.get());
            sender.sendMessage(STAFF_CHAT_SUCCESS.append(Component.text("Opération effectuée - " + levelArg.get() + " niveaux ajoutés à " + guildArg.get().getName())));
        }

        @Command(name = "addXp")
        public void addXp(@Argument("guild") GuildArg guildArg, @Argument("amount") IntegerCommandArgument xpArg, CommandSender sender) {
            guildService.addGuildXp(guildArg.get().getId(), xpArg.get());
            sender.sendMessage(STAFF_CHAT_SUCCESS.append(Component.text("Opération effectuée - " + xpArg.get() + " XP ajoutés à " + guildArg.get().getName())));
        }
    }
    @org.springframework.stereotype.Component
    @Command(name = "rank", description = "Gérer le rang d'une guilde")
    public static class RankCommand {
        private final GuildService guildService;

        public RankCommand(GuildService guildService) {
            this.guildService = guildService;
        }

        @Command(name = "set")
        public void setRank(@Argument("guild") GuildArg guildArg, @Argument("rank") GuildRankArg rankArg, CommandSender sender) {
            guildService.setRank(guildArg.get().getId(), rankArg.get());
            sender.sendMessage(STAFF_CHAT_SUCCESS.append(Component.text("Opération effectuée - rang de la guilde " + guildArg.get().getName() + " défini à " + rankArg.get())));
        }

        @Command(name = "get")
        public void getRank(@Argument("guild") GuildArg guildArg, CommandSender sender) {
            var guild = guildArg.get();
            sender.sendMessage(STAFF_CHAT_FORMAT.append(Component.text(guild.getName() + " : rang " + guild.getRank())));
        }
    }
}

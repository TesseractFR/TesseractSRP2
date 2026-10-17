package onl.tesseract.srp.controller.command.staff;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.commandBuilder.annotation.Argument;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.lib.command.argument.IntegerCommandArgument;
import onl.tesseract.lib.command.argument.PlayerArg;
import onl.tesseract.lib.menu.MenuService;
import onl.tesseract.srp.territory.adapter.userside.controller.commands.args.GuildArg;
import onl.tesseract.srp.territory.adapter.userside.controller.commands.args.GuildMembersRoleArg;
import onl.tesseract.srp.territory.adapter.userside.controller.commands.args.GuildMembersArg;
import onl.tesseract.srp.territory.adapter.userside.controller.commands.args.GuildRankArg;
import onl.tesseract.srp.territory.domain.port.serverside.PlayerGuildMessagingRepository;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

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
        var guild = guildArg.get();
        menuService.openConfirmationMenu(
                sender,
                Component.text("⚠ Es-tu sûr de vouloir supprimer la guilde " + guild.getName() + "?", NamedTextColor.RED),
                null,
                () -> guildService.deleteGuildAsStaff(sender.getUniqueId(), guild.getId())
        );
    }

    @Command(name = "members", description = "Gérer les membres d'une guilde")
    @org.springframework.stereotype.Component
    public static class MembersCommand {
        private final GuildService guildService;
        PlayerGuildMessagingRepository messaging;

        public MembersCommand(GuildService guildService) {
            this.guildService = guildService;
        }

        @Command(name = "get", description = "Voir les membres d'une guilde")
        public void getMembers(@Argument("guild") GuildArg guildArg, Player sender) {
            guildService.showMembersAsStaff(sender.getUniqueId(), guildArg.get().getId());
        }

        @Command(name = "add", description = "Ajouter un membre à une guilde")
        public void addMember(@Argument("guild") GuildArg guildArg, @Argument("playerName") PlayerArg playerArg, Player sender) {
            guildService.addMemberAsStaff(sender.getUniqueId(), guildArg.get().getId(), playerArg.get().getUniqueId());
        }

        @Command(name = "kick", description = "Expulser un joueur d'une guilde")
        public void kickMember(@Argument("guild") GuildArg guildArg, @Argument("playerName") GuildMembersArg playerArg, Player sender) {
            UUID target = UUID.fromString(playerArg.get());
            guildService.kickMemberAsStaff(sender.getUniqueId(), guildArg.get().getId(), target);
        }

        @Command(name = "setRole", description = "Définir le rôle d'un joueur dans une guilde. Si on rétrograde le leader actuel, indiquer [newLeader].")
        public void setRole(Player sender, @Argument("guild") GuildArg guildArg, @Argument("player") GuildMembersArg playerArg,
                            @Argument("role") GuildMembersRoleArg roleArg,
                            @Argument(value = "newLeader", optional = true) GuildMembersArg newLeaderArg) {
            var guild = guildArg.get();
            String playerName = playerArg.get();
            UUID targetId = guild.getMembers().stream()
                    .map(m -> Bukkit.getOfflinePlayer(m.getPlayerID()))
                    .filter(it -> {
                        String name = it.getName();
                        return (name != null && name.equalsIgnoreCase(playerName))
                                || it.getUniqueId().toString().equals(playerName);
                    })
                    .map(OfflinePlayer::getUniqueId)
                    .findFirst()
                    .orElse(null);

            UUID replacementLeaderId = newLeaderArg != null
                    ? Bukkit.getOfflinePlayer(newLeaderArg.get()).getUniqueId() : null;

            guildService.setMemberRoleAsStaff(sender.getUniqueId(), guild.getId(),
                    targetId, roleArg.get(), replacementLeaderId);
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
        public void giveMoney(@Argument("guild") GuildArg guildArg, @Argument("amount") IntegerCommandArgument amountArg, Player sender) {
            guildService.giveMoneyAsStaff(sender.getUniqueId(), guildArg.get().getId(), amountArg.get());
        }

        @Command(name = "take")
        public void takeMoney(@Argument("guild") GuildArg guildArg, @Argument("amount") IntegerCommandArgument amountArg, Player sender) {
            guildService.takeMoneyAsStaff(sender.getUniqueId(), guildArg.get().getId(), amountArg.get());
        }

        @Command(name = "get")
        public void getMoney(@Argument("guild") GuildArg guildArg, Player sender) {
            guildService.showMoneyAsStaff(sender.getUniqueId(), guildArg.get().getId());
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
        public void setLevel(@Argument("guild") GuildArg guildArg, @Argument("level") IntegerCommandArgument levelArg, Player sender) {
            guildService.setLevelAsStaff(sender.getUniqueId(), guildArg.get().getId(), levelArg.get());
        }

        @Command(name = "get")
        public void getLevel(@Argument("guild") GuildArg guildArg, Player sender) {
            guildService.showLevelAsStaff(sender.getUniqueId(), guildArg.get().getId());
        }

        @Command(name = "addLevel")
        public void addLevel(@Argument("guild") GuildArg guildArg, @Argument("amount") IntegerCommandArgument levelArg, Player sender) {
            guildService.addLevelAsStaff(sender.getUniqueId(), guildArg.get().getId(), levelArg.get());
        }

        @Command(name = "addXp")
        public void addXp(@Argument("guild") GuildArg guildArg, @Argument("amount") IntegerCommandArgument xpArg, Player sender) {
            guildService.addGuildXpAsStaff(sender.getUniqueId(), guildArg.get().getId(), xpArg.get());
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
        public void setRank(@Argument("guild") GuildArg guildArg, @Argument("rank") GuildRankArg rankArg, Player sender) {
            guildService.setRankAsStaff(sender.getUniqueId(), guildArg.get().getId(), rankArg.get());
        }

        @Command(name = "get")
        public void getRank(@Argument("guild") GuildArg guildArg, Player sender) {
            guildService.showRankAsStaff(sender.getUniqueId(), guildArg.get().getId());
        }
    }
}

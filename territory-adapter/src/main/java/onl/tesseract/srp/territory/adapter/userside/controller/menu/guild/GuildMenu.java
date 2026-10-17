package onl.tesseract.srp.territory.adapter.userside.controller.menu.guild;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import onl.tesseract.lib.chat.ChatEntryService;
import onl.tesseract.lib.itembuilder.ItemBuilder;
import onl.tesseract.lib.itembuilder.ItemLoreBuilder;
import onl.tesseract.lib.menu.Menu;
import onl.tesseract.lib.menu.MenuService;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.lib.util.Util;
import onl.tesseract.srp.common.domain.model.Coordinate;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.port.userside.TeleportationService;
import onl.tesseract.srp.territory.adapter.userside.controller.display.GuildRankDisplay;
import onl.tesseract.srp.territory.domain.model.guild.Guild;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildRank;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildRole;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import java.util.*;

public class GuildMenu extends GuildBaseMenu {
    private static final int UPGRADE = 11;
    private static final int INFO = 13;
    private static final int STRUCTURES = 15;
    private static final int TELEPORT = 19;
    private static final int MISSIONS = 21;
    private static final int BANK = 23;
    private static final int SHOPS = 25;
    private static final int MEMBERS = 29;
    private static final int PERMISSIONS = 31;
    private static final int REQUESTS = 33;

    private final UUID playerId;
    private final GuildService guildService;
    private final MenuService menuService;
    private final TeleportationService teleportService;
    private final ChatEntryService chatService;

    public GuildMenu(UUID playerId, GuildService guildService, MenuService menuService,
                     TeleportationService teleportService, ChatEntryService chatService, Menu previous) {
        super(MenuSize.Five, Component.text("Guilde"), previous);
        this.playerId = playerId;
        this.guildService = guildService;
        this.menuService = menuService;
        this.teleportService = teleportService;
        this.chatService = chatService;
    }

    @Override
    public void placeButtons(Player viewer) {
        Guild guild = guildService.getGuildByMember(viewer.getUniqueId());
        if (guild == null) { close(); return; }
        placeDecorations();
        addBank(guild, viewer);
        addMembers(viewer);
        addStructuresButton();
        addShopsButton();
        addMissionsButton();
        addPermissionsButton();
        addTeleport();
        addUpgrade(guild);
        addRequests(viewer);
        addInfo(guild);
        addBackButton();
        addCloseButton();
    }

    private void addBank(Guild guild, Player viewer) {
        GuildRole role = guild.getMemberRole(playerId);
        var lore = new ItemLoreBuilder().newline()
                .append("Compte : ", NamedTextColor.GRAY).append(guild.getMoney() + " Lys", NamedTextColor.GREEN)
                .newline().newline()
                .append("Clic gauche : ", NamedTextColor.GOLD).append("Déposer", NamedTextColor.GRAY);
        if (role.canWithdrawMoney()) {
            lore.newline().append("Clic droit : ", NamedTextColor.GOLD).append("Retirer", NamedTextColor.GRAY);
        }
        button(BANK, new ItemBuilder(Material.GOLD_INGOT).name("Banque de guilde", NamedTextColor.GOLD)
                .lore(lore.get()).build(), click -> {
            if (click == ClickType.LEFT) promptBank(viewer, true);
            else if (click == ClickType.RIGHT && role.canWithdrawMoney()) promptBank(viewer, false);
        });
    }

    private void promptBank(Player viewer, boolean deposit) {
        close();
        chatService.getChatEntry(viewer,
                Component.text("Montant à " + (deposit ? "déposer" : "retirer"), NamedTextColor.GREEN), input -> {
                    int amount;
                    try { amount = Integer.parseInt(input.trim()); }
                    catch (NumberFormatException _) { guildService.invalidAmount(playerId); return; }
                    boolean ok = deposit ? guildService.bankDeposit(playerId, amount)
                            : guildService.bankWithdraw(playerId, amount);
                    if (ok) new GuildMenu(playerId, guildService, menuService, teleportService, chatService, previous).open(viewer);
                });
    }

    private void addMembers(Player viewer) {
        button(MEMBERS, new ItemBuilder(Material.PLAYER_HEAD).name("Membres", NamedTextColor.GOLD)
                        .lore(new ItemLoreBuilder().append("Voir la liste des membres de la guilde", NamedTextColor.GRAY).get()).build(),
                _ -> new GuildMembersMenu(playerId, guildService, menuService, 0, this).open(viewer));
    }

    private void addStructuresButton() {
        button(STRUCTURES, new ItemBuilder(Material.PAPER).name("Structures d'artisanat", NamedTextColor.GOLD)
                        .lore(new ItemLoreBuilder().append("Voir les statistiques des structures d'artisanat de la guilde", NamedTextColor.GRAY).get()).build(),
                _ -> { /* TODO : menu des stats des structures d'artisanat */ });
    }

    private void addShopsButton() {
        button(SHOPS, new ItemBuilder(Material.CHEST).name("Shops", NamedTextColor.GOLD)
                        .lore(new ItemLoreBuilder().append("Voir les shops de la guilde", NamedTextColor.GRAY).get()).build(),
                _ -> { /* TODO : menu des shops */ });
    }

    private void addMissionsButton() {
        button(MISSIONS, new ItemBuilder(Material.OAK_SIGN).name("Tableau des missions", NamedTextColor.GOLD)
                        .lore(new ItemLoreBuilder().append("Voir le tableau des missions de la guilde", NamedTextColor.GRAY).get()).build(),
                _ -> { /* TODO : menu du tableau des missions */ });
    }

    private void addPermissionsButton() {
        button(PERMISSIONS, new ItemBuilder(Material.BOOK).name("Permissions", NamedTextColor.GOLD)
                        .lore(new ItemLoreBuilder().append("Gérer les permissions des membres de la guilde", NamedTextColor.GRAY).get()).build(),
                _ -> { /* TODO : menu des permissions */ });
    }

    private void addTeleport() {
        button(TELEPORT, new ItemBuilder(Material.ENDER_PEARL).name("Téléportation vers sa guilde", NamedTextColor.GOLD)
                        .lore(new ItemLoreBuilder().append("Se téléporter au spawn privé de sa guilde", NamedTextColor.GRAY).get()).build(),
                _ -> {
                    Coordinate destination = guildService.resolveSpawnDestination(playerId, null);
                    if (destination == null) return;
                    close();
                    teleportService.teleport(new PlayerId(playerId), destination,
                            () -> guildService.notifyTeleported(playerId, null));
                });
    }

    private void addUpgrade(Guild guild) {
        button(UPGRADE, new ItemBuilder(Material.DIAMOND_PICKAXE).name("Améliorer le rang de guilde", NamedTextColor.GOLD)
                .lore(upgradeLore(guild).get()).build(), _ -> {
            if (guildService.upgradeRank(playerId)) close();
        });
    }
    private ItemLoreBuilder upgradeLore(Guild guild) {
        GuildRank current = guild.getRank();
        GuildRank next = current.next();
        var lore = new ItemLoreBuilder().append("Rang actuel : ", NamedTextColor.GRAY)
                .append(GuildRankDisplay.title(current), GuildRankDisplay.color(current)).newline().newline();
        if (next == null) return lore.append("Votre guilde est déjà au rang maximum.", NamedTextColor.GOLD);
        boolean level = guild.getLevel() >= next.getMinLevel();
        boolean money = guild.getMoney() >= next.getCost();
        lore.append("Prochain rang : ", NamedTextColor.GRAY)
                .append(GuildRankDisplay.title(next), GuildRankDisplay.color(next)).newline()
                .append("Coût : ", NamedTextColor.GRAY)
                .append(next.getCost() + " Lys", money ? NamedTextColor.GREEN : NamedTextColor.RED)
                .newline().newline();
        if (!level) {
            lore.append("Niveau minimum requis : " + next.getMinLevel(), NamedTextColor.RED).newline()
                    .append("(Niveau actuel : " + guild.getLevel() + ")", NamedTextColor.RED, TextDecoration.ITALIC);
        } else if (!money) {
            lore.append("Argent insuffisant.", NamedTextColor.RED).newline()
                    .append("(Banque de guilde : " + guild.getMoney() + " Lys)", NamedTextColor.RED, TextDecoration.ITALIC);
        } else {
            lore.append("Cliquez pour améliorer la guilde !", NamedTextColor.GREEN);
        }
        return lore;
    }

    private void addRequests(Player viewer) {
        button(REQUESTS, new ItemBuilder(Material.CLOCK).name("Demandes pour rejoindre en attente", NamedTextColor.GOLD)
                        .lore(new ItemLoreBuilder().append("Voir les demandes de joueurs en attente", NamedTextColor.GRAY).get()).build(),
                _ -> new GuildJoinRequestsMenu(playerId, guildService, menuService, 0, this).open(viewer));
    }

    private void addInfo(Guild guild) {
        GuildRank rank = guild.getRank();
        var lore = new ItemLoreBuilder()
                .append("Nom : ", NamedTextColor.GRAY).append(guild.getName(), NamedTextColor.WHITE).newline()
                .append("Niveau : ", NamedTextColor.GRAY)
                .append(String.valueOf(guild.getLevel()), NamedTextColor.GREEN)
                .append(" (XP : ", NamedTextColor.GRAY, TextDecoration.ITALIC)
                .append(String.valueOf(guild.getXp()),
                        Util.getGreenRedGradient(guild.getXp(), guild.getXpForNextLevel()), TextDecoration.ITALIC)
                .append(" / " + guild.getXpForNextLevel() + ")", NamedTextColor.GRAY, TextDecoration.ITALIC)
                .newline()
                .append("Rang : ", NamedTextColor.GRAY)
                .append(GuildRankDisplay.title(rank), GuildRankDisplay.color(rank)).newline()
                .append("Chef : ", NamedTextColor.GRAY).append(playerName(guild.getLeaderId()), NamedTextColor.GOLD).newline()
                .append("Membres : ", NamedTextColor.GRAY)
                .append(String.valueOf(guild.getMembers().size()), NamedTextColor.AQUA)
                .append(" / " + rank.getMaxMembersNumber(), NamedTextColor.GRAY).newline()
                .append("Chunks : ", NamedTextColor.GRAY)
                .append(String.valueOf(guild.getChunks().size()), NamedTextColor.AQUA)
                .append(" / " + rank.getMaxChunksNumber(), NamedTextColor.GRAY);
        button(INFO, new ItemBuilder(Material.BLUE_BANNER).name("Informations générales", NamedTextColor.GOLD)
                .lore(lore.get()).build(), _ -> { });
    }
}

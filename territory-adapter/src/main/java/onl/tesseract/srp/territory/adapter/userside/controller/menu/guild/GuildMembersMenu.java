package onl.tesseract.srp.territory.adapter.userside.controller.menu.guild;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import onl.tesseract.lib.itembuilder.ItemBuilder;
import onl.tesseract.lib.itembuilder.ItemLoreBuilder;
import onl.tesseract.lib.menu.Menu;
import onl.tesseract.lib.menu.MenuService;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.srp.territory.adapter.userside.controller.display.GuildRoleDisplay;
import onl.tesseract.srp.territory.adapter.userside.controller.menu.DecoratedMenu;
import onl.tesseract.srp.territory.domain.model.guild.Guild;
import onl.tesseract.srp.territory.domain.model.guild.GuildMember;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildRole;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildRoleChange;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildService;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GuildMembersMenu extends DecoratedMenu {
    private static final int PAGE_SIZE = 45;
    private static final int PREV = 45;
    private static final int NEXT = 53;
    private static final DateTimeFormatter JOINED =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    private final UUID playerId;
    private final GuildService guildService;
    private final MenuService menuService;
    private final int page;

    public GuildMembersMenu(UUID playerId, GuildService guildService, MenuService menuService,
                            int page, Menu previous) {
        super(MenuSize.Six, Component.text("Membres de la guilde"), previous);
        this.playerId = playerId;
        this.guildService = guildService;
        this.menuService = menuService;
        this.page = page;
    }

    @Override
    public void placeButtons(Player viewer) {
        Guild guild = guildService.getGuildByMember(viewer.getUniqueId());
        if (guild == null) { close(); return; }
        List<GuildMember> members = sorted(guild);
        int current = clampPage(page, members.size(), PAGE_SIZE);
        boolean canManage = guild.getMemberRole(viewer.getUniqueId()).canInvite();

        int start = current * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, members.size());
        for (int i = start; i < end; i++) addMember(viewer, guild, i - start, members.get(i), canManage);

        addPagination(viewer, current, members.size(), PAGE_SIZE, PREV, NEXT,
                p -> new GuildMembersMenu(playerId, guildService, menuService, p, previous));
        addBackButton();
    }

    private List<GuildMember> sorted(Guild guild) {
        Comparator<GuildMember> cmp = Comparator
                .comparing((GuildMember m) -> !m.getPlayerID().equals(guild.getLeaderId()))
                .thenComparing(m -> -m.getRole().ordinal())
                .thenComparing(m -> m.getPlayerID().toString());
        return guild.getMembers().stream().sorted(cmp).toList();
    }

    private boolean canShowActions(Player viewer, Guild guild, GuildMember member, boolean canManage) {
        boolean viewerIsLeader = viewer.getUniqueId().equals(guild.getLeaderId());
        return canManage && !member.getPlayerID().equals(guild.getLeaderId())
                && (member.getRole() != GuildRole.Adjoint || viewerIsLeader);
    }

    private void addMember(Player viewer, Guild guild, int slot, GuildMember member, boolean canManage) {
        boolean actions = canShowActions(viewer, guild, member, canManage);
        GuildRole role = member.getRole();
        var lore = new ItemLoreBuilder()
                .append("Rôle : ", NamedTextColor.GRAY)
                .append(GuildRoleDisplay.of(role).getDisplayName(), GuildRoleDisplay.of(role).getColor()).newline()
                .append("A rejoint : ", NamedTextColor.GRAY)
                .append(JOINED.format(member.getJoinedDate()), NamedTextColor.WHITE).newline().newline();
        if (actions) {
            lore.append("Clic gauche : ", NamedTextColor.GOLD, TextDecoration.ITALIC)
                    .append("Promouvoir", NamedTextColor.GREEN, TextDecoration.ITALIC).newline()
                    .append("Clic droit : ", NamedTextColor.GOLD, TextDecoration.ITALIC)
                    .append(role == GuildRole.Citoyen ? "Exclure" : "Rétrograder",
                            NamedTextColor.RED, TextDecoration.ITALIC);
        }
        button(slot, new ItemBuilder(playerHead(member.getPlayerID()))
                .name(playerName(member.getPlayerID()), NamedTextColor.GOLD).lore(lore.get()).build(), click -> {
            if (!actions) return;
            if (click == ClickType.LEFT) promote(viewer, member);
            else if (click == ClickType.RIGHT) demoteOrKick(viewer, member);
        });
    }

    private void promote(Player viewer, GuildMember member) {
        UUID viewerId = viewer.getUniqueId();
        UUID target = member.getPlayerID();
        if (member.getRole() == GuildRole.Adjoint) {
            menuService.openConfirmationMenu(viewer,
                    Component.text("⚠ Promouvoir " + playerName(target) + " Chef ?", NamedTextColor.RED), this,
                    () -> { guildService.changeMemberRole(viewerId, target, GuildRoleChange.PROMOTE); reopen(viewer); });
            return;
        }
        if (guildService.changeMemberRole(viewerId, target, GuildRoleChange.PROMOTE)) reopen(viewer);
    }

    private void demoteOrKick(Player viewer, GuildMember member) {
        UUID viewerId = viewer.getUniqueId();
        UUID target = member.getPlayerID();
        if (member.getRole() == GuildRole.Citoyen) {
            if (!guildService.canKick(viewerId, target)) return;
            menuService.openConfirmationMenu(viewer,
                    Component.text("⚠ Exclure " + playerName(target) + " ?", NamedTextColor.RED), this,
                    () -> { guildService.kick(viewerId, target); reopen(viewer); });
            return;
        }
        if (guildService.changeMemberRole(viewerId, target, GuildRoleChange.DEMOTE)) reopen(viewer);
    }

    private void reopen(Player viewer) {
        new GuildMembersMenu(playerId, guildService, menuService, page, previous).open(viewer);
    }
}
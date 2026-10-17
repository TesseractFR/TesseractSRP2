package onl.tesseract.srp.territory.adapter.userside.controller.menu.guild;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import onl.tesseract.lib.itembuilder.ItemBuilder;
import onl.tesseract.lib.itembuilder.ItemLoreBuilder;
import onl.tesseract.lib.menu.Menu;
import onl.tesseract.lib.menu.MenuService;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.srp.common.adapter.utils.PlayerUtils;
import onl.tesseract.srp.territory.adapter.userside.controller.menu.DecoratedMenu;
import onl.tesseract.srp.territory.domain.model.guild.Guild;
import onl.tesseract.srp.territory.domain.model.guild.GuildJoinRequest;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildService;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import java.util.*;

public class GuildJoinRequestsMenu extends DecoratedMenu {
    private static final int PAGE_SIZE = 27;
    private static final int PREV = 18;
    private static final int NEXT = 26;
    private static final int MAX_MESSAGE = 120;

    private final UUID playerId;
    private final GuildService guildService;
    private final MenuService menuService;
    private final int page;

    public GuildJoinRequestsMenu(UUID playerId, GuildService guildService, MenuService menuService,
                                 int page, Menu previous) {
        super(MenuSize.Three, Component.text("Demandes en attente"), previous);
        this.playerId = playerId;
        this.guildService = guildService;
        this.menuService = menuService;
        this.page = page;
    }

    @Override
    public void placeButtons(Player viewer) {
        Guild guild = guildService.getGuildByMember(viewer.getUniqueId());
        if (guild == null) { close(); return; }
        boolean canManage = guild.getMemberRole(viewer.getUniqueId()).canInvite();
        List<GuildJoinRequest> requests = guild.getJoinRequests().stream()
                .sorted(Comparator.comparing(GuildJoinRequest::getRequestedDate).reversed()
                        .thenComparing(r -> r.getPlayerID().toString()))
                .toList();
        int current = clampPage(page, requests.size(), PAGE_SIZE);
        int start = current * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, requests.size());
        for (int i = start; i < end; i++) addRequest(viewer, i - start, requests.get(i), canManage);

        addPagination(viewer, current, requests.size(), PAGE_SIZE, PREV, NEXT,
                p -> new GuildJoinRequestsMenu(playerId, guildService, menuService, p, previous));
        addBackButton();
    }

    private void addRequest(Player viewer, int slot, GuildJoinRequest request, boolean canManage) {
        UUID target = request.getPlayerID();
        String name = playerName(target);
        String message = request.getMessage() == null ? "" : request.getMessage().trim();
        if (message.isBlank()) message = "—";
        else if (message.length() > MAX_MESSAGE) message = message.substring(0, MAX_MESSAGE - 1) + "…";

        var lore = new ItemLoreBuilder().append(message, NamedTextColor.WHITE, TextDecoration.ITALIC).newline();
        if (canManage) {
            lore.newline()
                    .append("Clic gauche : ", NamedTextColor.GOLD).append("Accepter", NamedTextColor.GREEN).newline()
                    .append("Clic droit : ", NamedTextColor.GOLD).append("Refuser", NamedTextColor.RED);
        }
        button(slot, new ItemBuilder(playerHead(target))
                .name(name, NamedTextColor.GOLD).lore(lore.get()).build(), click -> {
            if (!canManage) return;
            UUID viewerId = viewer.getUniqueId();
            if (click == ClickType.LEFT) {
                menuService.openConfirmationMenu(viewer, Component.text("Accepter la demande de " + name + " ?"), this,
                        () -> { guildService.acceptJoinRequest(viewerId, target); reopen(viewer); });
            } else if (click == ClickType.RIGHT) {
                menuService.openConfirmationMenu(viewer, Component.text("Refuser la demande de " + name + " ?"), this,
                        () -> { guildService.declineJoinRequest(viewerId, target); reopen(viewer); });
            }
        });
    }

    private void reopen(Player viewer) {
        new GuildJoinRequestsMenu(playerId, guildService, menuService, page, previous).open(viewer);
    }
}
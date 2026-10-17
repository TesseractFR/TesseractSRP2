package onl.tesseract.srp.territory.adapter.userside.controller.menu.campement;

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
import onl.tesseract.srp.territory.domain.model.campement.Campement;
import onl.tesseract.srp.territory.domain.port.userside.campement.CampementService;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class CampementTrustedPlayersMenu extends DecoratedMenu {
    private static final int PAGE_SIZE = 27;
    private static final int PREV = 27;
    private static final int NEXT = 35;

    private final UUID playerId;
    private final CampementService campementService;
    private final MenuService menuService;
    private final int page;

    public CampementTrustedPlayersMenu(UUID playerId, CampementService campementService,
                                       MenuService menuService, int page, Menu previous) {
        super(MenuSize.Three, Component.text("Joueurs de confiance"), previous);
        this.playerId = playerId;
        this.campementService = campementService;
        this.menuService = menuService;
        this.page = page;
    }

    @Override
    public void placeButtons(Player viewer) {
        Campement campement = campementService.getCampementByOwner(playerId);
        if (campement == null) { close(); return; }
        List<UUID> trusted = campement.getTrusted().stream()
                .sorted(Comparator.comparing(DecoratedMenu::playerName)).toList();
        int current = clampPage(page, trusted.size(), PAGE_SIZE);
        boolean owner = viewer.getUniqueId().equals(playerId);

        int start = current * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, trusted.size());
        for (int i = start; i < end; i++) addTrusted(viewer, i - start, trusted.get(i), owner);

        addPagination(viewer, current, trusted.size(), PAGE_SIZE, PREV, NEXT,
                p -> new CampementTrustedPlayersMenu(playerId, campementService, menuService, p, previous));
        addBackButton();
    }

    private void addTrusted(Player viewer, int slot, UUID uuid, boolean owner) {
        String name = playerName(uuid);
        var lore = new ItemLoreBuilder().append("Joueur de confiance", NamedTextColor.GRAY);
        if (owner) {
            lore.newline().append("Clic droit : ", NamedTextColor.GOLD, TextDecoration.ITALIC)
                    .append("Retirer", NamedTextColor.RED, TextDecoration.ITALIC);
        }
        button(slot, new ItemBuilder(playerHead(uuid))
                .name(name, NamedTextColor.GOLD).lore(lore.get()).build(), click -> {
            if (owner && click == ClickType.RIGHT) {
                menuService.openConfirmationMenu(viewer,
                        Component.text("⚠ Retirer " + name + " de la liste de confiance ?", NamedTextColor.RED), this,
                        () -> {
                            campementService.untrustPlayer(playerId, uuid);
                            new CampementTrustedPlayersMenu(playerId, campementService, menuService, page, previous).open(viewer);
                        });
            }
        });
    }
}

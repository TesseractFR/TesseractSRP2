package onl.tesseract.srp.territory.adapter.userside.controller.menu.campement;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.lib.itembuilder.ItemBuilder;
import onl.tesseract.lib.itembuilder.ItemLoreBuilder;
import onl.tesseract.lib.menu.Menu;
import onl.tesseract.lib.menu.MenuService;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.srp.common.domain.model.Coordinate;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.port.userside.TeleportationService;
import onl.tesseract.srp.territory.domain.model.campement.Campement;
import onl.tesseract.srp.territory.domain.port.userside.campement.CampementService;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CampementMenu extends CampementBaseMenu {
    private static final int TELEPORT = 10;
    private static final int INFO = 12;
    private static final int TRUSTED = 14;
    private static final int SHOPS = 16;

    private final UUID playerId;
    private final CampementService campementService;
    private final MenuService menuService;
    private final TeleportationService teleportService;

    public CampementMenu(UUID playerId, CampementService campementService, MenuService menuService,
                         TeleportationService teleportService, Menu previous) {
        super(MenuSize.Three, Component.text("Campement"), previous);
        this.playerId = playerId;
        this.campementService = campementService;
        this.menuService = menuService;
        this.teleportService = teleportService;
    }

    @Override
    public void placeButtons(Player viewer) {
        Campement campement = campementService.getCampementByOwner(playerId);
        if (campement == null) { close(); return; }
        placeDecorations();

        button(TELEPORT, new ItemBuilder(Material.ENDER_PEARL).name("Téléportation", NamedTextColor.GOLD)
                        .lore(new ItemLoreBuilder().append("Se téléporter au spawn de son campement", NamedTextColor.GRAY).get()).build(),
                _ -> {
                    Coordinate destination = campementService.resolveSpawnDestination(playerId, null);
                    if (destination == null) return;
                    close();
                    teleportService.teleport(new PlayerId(playerId), destination,
                            () -> campementService.notifyTeleported(playerId, null));
                });

        var lore = new ItemLoreBuilder()
                .append("Propriétaire : ", NamedTextColor.GRAY)
                .append(playerName(campement.getOwnerID()), NamedTextColor.GOLD).newline()
                .append("Niveau : ", NamedTextColor.GRAY)
                .append(String.valueOf(campement.getCampLevel()), NamedTextColor.GREEN).newline()
                .append("Nombre de chunks : ", NamedTextColor.GRAY)
                .append(String.valueOf(campement.getChunks().size()), NamedTextColor.AQUA);
        button(INFO, new ItemBuilder(Material.BLUE_BANNER).name("Informations générales", NamedTextColor.GOLD)
                .lore(lore.get()).build(), _ -> { });

        button(TRUSTED, new ItemBuilder(Material.PLAYER_HEAD).name("Joueurs de confiance", NamedTextColor.GOLD)
                        .lore(new ItemLoreBuilder().append("Gérer les joueurs autorisés sur votre campement", NamedTextColor.GRAY).get()).build(),
                _ -> new CampementTrustedPlayersMenu(playerId, campementService, menuService, 0, this).open(viewer));

        button(SHOPS, new ItemBuilder(Material.CHEST).name("Shops", NamedTextColor.GOLD)
                        .lore(new ItemLoreBuilder().append("Voir les shops du campement", NamedTextColor.GRAY).get()).build(),
                _ -> { /* TODO : shops de campements */ });

        addBackButton();
        addCloseButton();
    }
}
